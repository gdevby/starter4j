package by.gdev.http.head.cache.service;

import java.io.IOException;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

/**
 * Saved files in cache and got from it
 */
public interface FileCacheService {
	/**
	 * @param url - url address
	 * @param cache - If cache = true file exists and hashsum is valid it should
	 *              return content without head request. 
	 *              If cache false we need to do http head request to check version in the cache with ETag
	 * @return path which contains response of the http get request
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	Path getRawObject(String url, boolean cache) throws IOException, NoSuchAlgorithmException;
}
