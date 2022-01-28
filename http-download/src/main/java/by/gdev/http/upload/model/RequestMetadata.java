package by.gdev.http.upload.model;

import lombok.Data;

@Data
public class RequestMetadata {
	String contentLength;
	String lastModified;
	String eTag;
	String sha1;
}
