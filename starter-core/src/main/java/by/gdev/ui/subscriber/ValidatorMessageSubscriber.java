package by.gdev.ui.subscriber;

import javax.swing.JOptionPane;

import com.google.common.eventbus.Subscribe;

import by.gdev.model.ValidationExceptionMessage;
import by.gdev.ui.StarterStatusFrame;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidatorMessageSubscriber {
	StarterStatusFrame frame;

	@Subscribe
	public void message(ValidationExceptionMessage s) {
		JOptionPane.showMessageDialog(frame, s.getMessage(), "", JOptionPane.ERROR_MESSAGE);
	}
}
