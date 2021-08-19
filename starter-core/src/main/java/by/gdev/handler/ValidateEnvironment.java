package by.gdev.handler;


public interface ValidateEnvironment {

	/**
	 * Этот метод валидирует один случай
	 * @return
	 */
	boolean valite();
	/**
	 * возвращает сообщение для показа и после будет генерирывать события для busevent
	 * @return
	 */
	String getExceptionMessage();
}
