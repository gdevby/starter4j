package by.gdev.ui.subscriber;

import javax.swing.JOptionPane;

import com.google.common.eventbus.Subscribe;

import by.gdev.model.ExceptionMessage;
import by.gdev.ui.StarterStatusFrame;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidatorMessageSubscriber {
	StarterStatusFrame frame;

	@Subscribe
	public void message(ExceptionMessage s) {
		JOptionPane.showMessageDialog(frame, s.getMessage(), "", JOptionPane.ERROR_MESSAGE);
	}
}
