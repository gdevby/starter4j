package by.gdev.http.upload.service;

import java.io.IOException;
import java.nio.file.Path;

import by.gdev.http.upload.model.RequestMetadata;
/**
 * 
 * This service sent http get, http head request and saved response to file
 *
 */
public interface HttpService {
	/**
	 * GET request
	 * @param url - url address 
	 * @param path - saved content to this path
	 * @return RequestMetadata metadata about url
	 * @throws IOException
	 */
	RequestMetadata getRequestByUrlAndSave(String url, Path path) throws IOException;
	/**
	 * HEAD request
	 * @param url - url address 
	 * @return RequestMetadata metadata about url
	 * @throws IOException
	 */
	RequestMetadata getMetaByUrl(String url) throws IOException;
}