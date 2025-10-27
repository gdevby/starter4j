package by.gdev.http.download.model;

import lombok.Data;

/**
 * Class for describing the metadata of the uploaded file
 * @author Robert Makrytski 
 */
@Data
public class RequestMetadata {
	private String lastModified;
	private String eTag;
	private String sha1;
	private long lastAccessAt;
}
