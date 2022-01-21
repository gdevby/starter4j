package by.gdev.http.upload.exeption;

public class HashSumError extends UploadFileException {
	private static final long serialVersionUID = 6549216849433173596L;

	public HashSumError(String uri, String localPath, String message) {
		super(uri, localPath, message);
	}
}
