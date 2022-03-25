package by.gdev.util.excepiton;

import java.io.IOException;

public class NotAllowWriteFileOperation extends IOException{
	private static final long serialVersionUID = -4010731996168068063L;

	public NotAllowWriteFileOperation(String message) {
		super(message);
	}
}
