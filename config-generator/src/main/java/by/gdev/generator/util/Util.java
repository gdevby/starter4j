package by.gdev.generator.util;

import java.util.function.Function;
//todo could we create one util in any other project and use this methods from that
public class Util {

	public static <T, R> Function<T, R> wrap(CheckedFunction<T, R> checkedFunction) {
		return t -> {
			try {
				return checkedFunction.apply(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static void sleep(int seconds) {
		try {
			Thread.sleep(1000 * seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
