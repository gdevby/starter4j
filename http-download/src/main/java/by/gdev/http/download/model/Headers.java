package by.gdev.http.download.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Class for describing request response headers
 * @author Robert Makrytski 
 */

@Getter
@RequiredArgsConstructor
public enum Headers {
	SHA1("SHA-1"), 
	ETAG("ETag"), 
	CONTENTLENGTH("Content-Length"), 
	LASTMODIFIED("Last-Modified");

	private final String value;
}
