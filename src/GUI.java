import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class GUI {
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		new GUI();
	}
	
	private Set<Clone> cloneCache = new HashSet<Clone>();
	private Stack<Clone> toValidate = new Stack<Clone>();
	private Set<Clone> validated = new HashSet<Clone>();
	
	private Path inputFile;
	private Path resultFile;
	private JFrame frame;
	
	Clone currentClone = null;
	
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
		} while (retval != JFileChooser.APPROVE_OPTION);
		
// Read Validated Clones In
		{
			ResultsFileReader rfr = new ResultsFileReader(resultFile);
			Clone readClone;
			while((readClone = rfr.nextClone()) != null) {
				validated.add(readClone);
			}
			rfr.close();
		}
		
// Read Clones In, and determine unvaldiated ones
		CloneFileReader cfr = new CloneFileReader(inputFile);
		{
			Clone readClone;
			while((readClone = cfr.nextClone()) != null) {
				cloneCache.add(readClone);
				if(!validated.contains(cloneCache)) {
					toValidate.add(readClone);
				}
			}
		}
		
// Setup GUI
		frame = new JFrame("Java Oracle");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
	// Fragment Display
		JPanel fragments = new JPanel();
		fragments.setLayout(new GridLayout(1,2));
		
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
		
		fragments.add(f1_scroll);
		fragments.add(f2_scroll);
		frame.add(fragments, BorderLayout.CENTER);
		
	// Controls
		JButton tp_button = new JButton("True Positive");
		JButton fp_button = new JButton("False Positive");
		
		JPanel textEditPanel = new JPanel();
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
				if(currentClone != null) {
					f1.setText(currentClone.getText1());
					f2.setText(currentClone.getText2());
				}
			}
		});
		textEditPanel.add(reset);
		textEditPanel.add(lock);
		textEditPanel.add(unlock);
		
		clone = new JLabel("");
		JPanel bottomP = new JPanel();
		bottomP.setLayout(new GridLayout(3,1));
		frame.add(tp_button, BorderLayout.PAGE_START);
		frame.add(bottomP,BorderLayout.PAGE_END);
		bottomP.add(textEditPanel);
		bottomP.add(fp_button);
		bottomP.add(clone);
		
		tp_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					numTP++;
					currentClone.setValidation(true);
					validated.add(currentClone);
					update();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		fp_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					numFP++;
					currentClone.setValidation(false);
					validated.add(currentClone);
					update();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		frame.setSize(500, 500);
		frame.setVisible(true);
		update();
	}
	
	private void update() throws IOException {
		if(toValidate.size() == 0) {
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("Final Talley:");
			System.out.println("#TP = " + numTP);
			System.out.println("#FP = " + numFP);
			System.out.flush();
			writeout();
			System.exit(0);
		}
		currentClone = toValidate.pop();
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("Current Talley:");
		System.out.println("#TP = " + numTP);
		System.out.println("#FP = " + numFP);
		System.out.flush();
		f1.setText(currentClone.getText1());
		f2.setText(currentClone.getText2());
		clone.setText(currentClone.getFragment1() + "                                  " + currentClone.getFragment2());
		if(isOverlap()) {
			clone.setForeground(Color.red);
		} else {
			clone.setForeground(Color.black);
		}
	}
	
	private void writeout() throws IOException {
		ResultsFileWriter rfw = new ResultsFileWriter(this.resultFile);
		for(Clone clone : this.validated) {
			rfw.write(clone);
		}
		rfw.close();
	}

	private boolean isOverlap() {
		String f1 = currentClone.getFragment1();
		String f2 = currentClone.getFragment2();
		
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
	
	
}
