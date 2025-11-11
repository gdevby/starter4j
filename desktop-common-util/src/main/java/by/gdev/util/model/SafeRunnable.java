package by.gdev.util.model;

@FunctionalInterface
public interface SafeRunnable {
	void run() throws Exception;
}
