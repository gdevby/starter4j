package by.gdev.handler;

import java.io.IOException;

import by.gdev.model.ExceptionMessage;

public interface ValidateEnvironment {

	/**
	 * This method validates a specific case
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	boolean validate();
	/**
	 * Returns an error message
	 * @return
	 */
	ExceptionMessage getExceptionMessage();
}
