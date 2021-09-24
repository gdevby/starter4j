package http.head.get.hash.model;

import lombok.Data;

@Data
public class RequestMetadata {
	String contentLength;
	String lastModified;
	String eTag;
}
