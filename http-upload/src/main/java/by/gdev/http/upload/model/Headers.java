package by.gdev.http.upload.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Headers {
	SHA1("SHA-1"), 
	ETAG("ETag"), 
	CONTENTLENGTH("Content-Length"), 
	LASTMODIFIED("Last-Modified");

	private final String value;
}
