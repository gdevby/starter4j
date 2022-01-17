package by.gdev.handler;

import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;

import by.gdev.model.ValidationExceptionMessage;

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
	boolean validate();
	/**
	 * возвращает сообщение для показа и после будет генерирывать события для busevent
	 * @return
	 */
	//TODO create type ValidationExceptionMessage and used for eventBus.post(val.getExceptionMessage());
	ValidationExceptionMessage getExceptionMessage();
}
