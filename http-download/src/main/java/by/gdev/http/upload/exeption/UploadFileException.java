package by.gdev.http.upload.exeption;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * TODO
 * @author Robert Makrytski
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UploadFileException extends IOException {
	private static final long serialVersionUID = -2684927056566219164L;
	private String uri;
	private String localPath;
	public UploadFileException(String uri, String localPath, String message) {
		super(message);
		this.uri = uri;
		this.localPath = localPath;
	}
	
}
