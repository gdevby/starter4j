package by.gdev.http.download.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import by.gdev.http.download.model.RequestMetadata;

/**
 * 
 * This service sent http get, http head request and saved response to file
 *
 */

public interface HttpService {
	/**
	 * GET request
	 * 
	 * @param uri  - uri address
	 * @param path - saved content to this path
	 * @return RequestMetadata metadata about url
	 * @throws IOException
	 */
	RequestMetadata getRequestByUrlAndSave(String uri, Path path) throws IOException;

	/**
	 * HEAD request
	 * 
	 * @param uri - uri address
	 * @return RequestMetadata metadata about url
	 * @throws IOException
	 */
	RequestMetadata getMetaByUrl(String uri) throws IOException;

	/**
	 * Returns the contents of a file without saving it
	 * 
	 * @param url uri address
	 * @return
	 * @throws IOException
	 */
	String getRequestByUrl(String url) throws IOException;

	String getRequestByUrl(String url, Map<String, String> map) throws IOException;
}