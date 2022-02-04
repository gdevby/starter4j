package by.gdev.http.download.model;

import lombok.Data;
//TODO
@Data
public class RequestMetadata {
	private String contentLength;
	private String lastModified;
	private String eTag;
	private String sha1;
}
