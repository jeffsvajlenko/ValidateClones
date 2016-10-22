import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GUI {
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		new GUI();
	}
	
	private Map<Clone,Clone> cloneCache = new HashMap<Clone,Clone>();
	private List<Clone> clones = new ArrayList<Clone>();
	
	private Path inputFile;
	private Path resultFile;
	private JFrame frame;
	private JList<Clone> list;
	private DefaultListModel<Clone> listModel;
	
	private JButton tp_button;
	private JButton ud_button;
	private JButton fp_button;
	
	private JCheckBox autonext = new JCheckBox("Auto-Next",true);
	
	int currentIndex = -1;
	
	JLabel clone;
	RSyntaxTextArea f1;
	RTextScrollPane f1_scroll;
	RSyntaxTextArea f2;
	RTextScrollPane f2_scroll;
	
	int numTP = 0;
	int numFP = 0;
	
	public GUI() throws IOException {
		int retval;
		JFileChooser fc = new JFileChooser();
		
// Get Files
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select input file.");
		do {
			retval = fc.showOpenDialog(null);
			inputFile = fc.getSelectedFile().toPath();
			if(retval == JFileChooser.CANCEL_OPTION)
				System.exit(0);
		} while (retval != JFileChooser.APPROVE_OPTION);
		
		fc.setDialogTitle("Select output file for results.");
		do {
			retval = fc.showOpenDialog(null);
			resultFile = fc.getSelectedFile().toPath();
			if(retval == JFileChooser.CANCEL_OPTION)
				System.exit(0);
			if(!Files.exists(resultFile)) {
				Files.createFile(resultFile);
			}
		} while (retval != JFileChooser.APPROVE_OPTION);
	
// Read Clones, Store in Cache and Build Validation List
		CloneFileReader cfr = new CloneFileReader(inputFile);
		{
			int i = 0;
			Clone readClone;
			while((readClone = cfr.nextClone()) != null) {
				cloneCache.put(readClone,readClone);
				clones.add(readClone);
				readClone.setIndex(i++);
			}
		}
		
// Read validate clones, update the cached with validation status
		{
			ResultsFileReader rfr = new ResultsFileReader(resultFile);
			Clone readClone;
			while((readClone = rfr.nextClone()) != null) {
				cloneCache.get(readClone).setValidation(readClone.getValidation());
			}
			rfr.close();
		}
		
// Setup GUI
		frame = new JFrame("Java Oracle");
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent e) {
			}
			@Override
			public void windowClosed(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					writeout();
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(-1);
				}
				System.exit(0);
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowOpened(WindowEvent e) {
			}
		});
		
		JSplitPane middlePane = new JSplitPane();
		JSplitPane fragmentsPane = new JSplitPane();
		
		
	// Clone List
		JScrollPane listPanel = new JScrollPane();
		listPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listModel = new DefaultListModel<Clone>();
		for(Clone clone : clones) {
			listModel.addElement(clone);
		}
		list = new JList<Clone>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new CloneCellRenderer());
		listPanel.setViewportView(list);
		middlePane.setLeftComponent(listPanel);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				currentIndex = list.getSelectedIndex();
				update();
			}
		});
		
		
	// Fragment Display
		
		f1 = new RSyntaxTextArea();
		f1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		f1.setEditable(false);
		f1.setText("");
		f1.setHighlightCurrentLine(false);
		f1_scroll = new RTextScrollPane(f1);
		f1_scroll.setLineNumbersEnabled(true);
		
		f2 = new RSyntaxTextArea();
		f2.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		f2.setEditable(false);
		f2.setText("");
		f2.setHighlightCurrentLine(false);
		f2_scroll = new RTextScrollPane(f2);
		f2_scroll.setLineNumbersEnabled(true);	
		fragmentsPane.setLeftComponent(f1_scroll);
		fragmentsPane.setRightComponent(f2_scroll);
		middlePane.setRightComponent(fragmentsPane);
		
	// Controls
		
		// Switch Panel
		JPanel switchpanel = new JPanel();
		JButton next = new JButton(">");
		JButton nextV = new JButton(">>");
		JButton prev = new JButton("<");
		JButton prevV = new JButton("<<");
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				currentIndex++;
				if(currentIndex >= clones.size()) {
					currentIndex=0;
				}
				update();
			}
		});
		prev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentIndex--;
				if(currentIndex < 0)
					currentIndex=clones.size()-1;
				update();
			}
		});
		nextV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextV();
			}
		});
		prevV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int initialIndex = currentIndex;
				do {
					currentIndex--;
					if(currentIndex < 0)
						currentIndex = clones.size()-1;
				} while (currentIndex != initialIndex && clones.get(currentIndex).getValidation() != null);
				update();
			}
		});
		switchpanel.add(prevV);
		switchpanel.add(prev);
		switchpanel.add(next);
		switchpanel.add(nextV);
		frame.add(switchpanel,BorderLayout.NORTH);
		
		// Text Edit Panel
		JPanel textEditPanel = new JPanel();
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					writeout();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton lock = new JButton("Lock");
		lock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				f1.setEditable(false);
				f2.setEditable(false);
			}
		});
		JButton unlock = new JButton("Unlock");
		unlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				f1.setEditable(true);
				f2.setEditable(true);
			}
		});
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		textEditPanel.add(save);
		textEditPanel.add(reset);
		textEditPanel.add(lock);
		textEditPanel.add(unlock);
		textEditPanel.add(autonext);
		
		// Validate Panel
		tp_button = new JButton("True Positive");
		ud_button = new JButton("Undecided");
		fp_button = new JButton("False Positive");
		JPanel validatePanel = new JPanel();
		tp_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clones.get(currentIndex).setValidation(true);
				if(autonext.isSelected())
					nextV();
				update();
			}
		});
		ud_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clones.get(currentIndex).setValidation(null);
				if(autonext.isSelected())
					nextV();
				update();
			}
		});
		fp_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clones.get(currentIndex).setValidation(false);
				if(autonext.isSelected())
					nextV();
				update();
			}
		});
		validatePanel.add(tp_button);
		validatePanel.add(ud_button);
		validatePanel.add(fp_button);
		
		clone = new JLabel("");
		JPanel bottomP = new JPanel();
		bottomP.setLayout(new GridLayout(3,1));
		bottomP.add(textEditPanel);
		bottomP.add(validatePanel);
		bottomP.add(clone);
		frame.add(bottomP, BorderLayout.SOUTH);
		frame.add(middlePane, BorderLayout.CENTER);
		
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setSize(1000, 1000);
		middlePane.setDividerLocation(0.2);
		middlePane.setResizeWeight(0.05);
		fragmentsPane.setDividerLocation(0.5);
		fragmentsPane.setResizeWeight(0.5);
		frame.setVisible(true);
		this.currentIndex = 0;
		
		update();
	}
	
	private void update() {
		Clone currentClone = clones.get(currentIndex);
		list.setSelectedIndex(currentClone.getIndex());
		f1.setText(currentClone.getText1());
		f2.setText(currentClone.getText2());
		clone.setText(currentClone.getFragment1() + "                                  " + currentClone.getFragment2());
		
		tp_button.setBackground(Color.LIGHT_GRAY);
		ud_button.setBackground(Color.LIGHT_GRAY);
		fp_button.setBackground(Color.LIGHT_GRAY);
		
		if(currentClone.getValidation() == null) {
			ud_button.setBackground(Color.GREEN);
		} else if (currentClone.getValidation() == true) {
			tp_button.setBackground(Color.GREEN);
		} else {
			fp_button.setBackground(Color.GREEN);
		}
		
		if(isOverlap()) {
			clone.setForeground(Color.red);
		} else {
			clone.setForeground(Color.black);
		}
		list.ensureIndexIsVisible(currentIndex);
	}
	
	private void writeout() throws IOException {
		ResultsFileWriter rfw = new ResultsFileWriter(this.resultFile);
		for(Clone clone : this.clones) {
			rfw.write(clone);
		}
		rfw.close();
	}

	private boolean isOverlap() {
		String f1 = clones.get(currentIndex).getFragment1();
		String f2 = clones.get(currentIndex).getFragment2();
		
		String parts[] = f1.split(" ");
		String src1 = parts[0];
		int start1 = Integer.parseInt(parts[1]);
		int end1 = Integer.parseInt(parts[2]);
		
		parts = null;
		
		parts = f2.split(" ");
		String src2 = parts[0];
		int start2 = Integer.parseInt(parts[1]);
		int end2 = Integer.parseInt(parts[2]);
		
		System.out.println(src1);
		System.out.println(src2);
		
		
		if(src1.equals(src2)) {
			System.out.println("same file");
			System.out.println(start1 + " " + start2);
			System.out.println(end1 + " " + end2);
			if(start1 >= start2 && start1 <= end2) // 1 starts within 2
				return true;
			else if (end1 >= start2 && end1 <= end2) // 1 ends within 2
				return true;
		}
		return false;
	}
	
	public void nextV() {
		int initialIndex = currentIndex;
		do {
			currentIndex++;
			if(currentIndex >= clones.size())
				currentIndex=0;
		} while(currentIndex != initialIndex && clones.get(currentIndex).getValidation() != null);
		update();
	}
	
}
