package by.gdev.http.download.service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;

/**
 * Saved files in cache and got from it
 */
public interface FileCacheService {
	/**
	 * @param uri - uri address
	 * @param cache - If cache = true file exists and hashsum is valid it should
	 *              return content without head request. 
	 *              If cache false we need to do http head request to check version in the cache with ETag
	 * @return path which contains response of the http get request
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */

	Path getRawObject(String uri, boolean cache) throws IOException, NoSuchAlgorithmException;
	/**
	 * Allowed to download small files and used without GsonService 
	 * @param cache - {@link FileCacheService#getRawObject(String, boolean)}
	 * @param urls Got array url from {@link Repo}
	 * @param metadata - {@link Metadata} 
	 * @return Path - which contains file
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	Path getRawObject(List<String> urls, Metadata metadata, boolean cache)throws IOException, NoSuchAlgorithmException;
}
