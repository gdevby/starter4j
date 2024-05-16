package by.gdev.ui;

import javax.swing.JLabel;

public class JLabelHtmlWrapper extends JLabel {

	private static final long serialVersionUID = 2703012842940047505L;

	public JLabelHtmlWrapper(String s) {
		setText(s);
	}

	@Override
	public void setText(String text) {
		super.setText("<html>" + text + "</html>");
	}
}
