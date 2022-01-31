package by.gdev.http.upload.exeption;
/**
 * Notifies you when there are errors in the loading state
 * @author Robert Makrytski
 *
 */
@SuppressWarnings("serial")
public class StatusExeption extends Exception{
	
	public StatusExeption(String status) {
		super(status);
	}

}
