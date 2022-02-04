package by.gdev.http.upload.model;

import lombok.Data;
//TODO
@Data
public class RequestMetadata {
	//TODO private???
	String contentLength;
	String lastModified;
	String eTag;
	String sha1;
}
