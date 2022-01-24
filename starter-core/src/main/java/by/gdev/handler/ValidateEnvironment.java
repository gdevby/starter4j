package by.gdev.handler;

import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;

import by.gdev.model.ValidationExceptionMessage;

public interface ValidateEnvironment {

	/**
	 * This method validates a specific case
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	boolean validate();
	/**
	 * Returns an error message
	 * @return
	 */
	ValidationExceptionMessage getExceptionMessage();
}
