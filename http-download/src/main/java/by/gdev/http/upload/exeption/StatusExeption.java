package by.gdev.http.upload.exeption;

@SuppressWarnings("serial")
public class StatusExeption extends Exception{
	
	public StatusExeption(String status) {
		super(status);
	}

}
