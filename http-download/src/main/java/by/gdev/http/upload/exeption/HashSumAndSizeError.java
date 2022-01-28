package by.gdev.http.upload.exeption;


public class HashSumAndSizeError extends UploadFileException {
	private static final long serialVersionUID = 6549216849433173596L;

	public HashSumAndSizeError(String uri, String localPath, String message) {
		super(uri, localPath, message);
	}
}
