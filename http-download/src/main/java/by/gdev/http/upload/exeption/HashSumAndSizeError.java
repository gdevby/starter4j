package by.gdev.http.upload.exeption;

/**
 * This exception is thrown when the uploaded file does not match the size or hash amount specified in the config file
 * @author Robert Makrytski
 *
 */
public class HashSumAndSizeError extends UploadFileException {
	private static final long serialVersionUID = 6549216849433173596L;

	public HashSumAndSizeError(String uri, String localPath, String message) {
		super(uri, localPath, message);
	}
}
