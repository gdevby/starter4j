package by.gdev.ui;

import javax.swing.JOptionPane;

import com.google.common.eventbus.Subscribe;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public class ValidatorMessageSubscriber {
	StarterStatusFrame frame;
	@Subscribe
    public void message(String s) {
    	JOptionPane.showMessageDialog(frame, s, "", JOptionPane.ERROR_MESSAGE);
    }

}
