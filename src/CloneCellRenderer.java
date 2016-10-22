import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CloneCellRenderer extends JLabel implements ListCellRenderer<Clone> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Clone> list, Clone value, int index, boolean isSelected, boolean cellHasFocus) {
		String text = Integer.toString(value.getIndex()) + " ";
		if(value.getValidation() == null) {
			text += "  *** ";
		} else {
			
		}
		this.setText(text);
		
		
		this.setOpaque(true);
		if(isSelected) {
			this.setBackground(Color.LIGHT_GRAY);
		} else {
			this.setBackground(Color.WHITE);
		}
		
		
		
		return this;
	}

}
