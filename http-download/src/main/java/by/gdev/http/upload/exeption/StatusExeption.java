package by.gdev.http.upload.exeption;
/**
 * TODO
 * @author Robert Makrytski
 *
 */
@SuppressWarnings("serial")
public class StatusExeption extends Exception{
	
	public StatusExeption(String status) {
		super(status);
	}

}
