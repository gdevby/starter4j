package by.gdev.http.head.cache.model;

import lombok.Data;

@Data
public class RequestMetadata {
	String contentLength;
	String lastModified;
	String eTag;
	String sha1;
}
