package by.gdev.handler;

import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;

public interface ValidateEnvironment {

	/**
	 * Этот метод валидирует один случай
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	
	boolean validate() throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException;
	/**
	 * возвращает сообщение для показа и после будет генерирывать события для busevent
	 * @return
	 */
	String getExceptionMessage();
}
