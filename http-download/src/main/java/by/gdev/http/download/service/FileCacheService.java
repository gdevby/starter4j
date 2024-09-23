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
	 * Allowed to download small files and used without GsonService
	 * 
	 * @param cache    - {@link FileCacheService#getRawObject(String, boolean)}
	 * @param urls     Get array url from {@link Repo}
	 * @param urn      related path
	 * @param metadata - {@link Metadata}
	 * @return Path - which contains file
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	Path getRawObject(List<String> urls, String urn, boolean cache) throws IOException, NoSuchAlgorithmException;

	/**
	 * @see FileCacheService#getRawObject(List, String, boolean)
	 */
	Path getRawObject(List<String> urls, Metadata metadata, boolean cache) throws IOException, NoSuchAlgorithmException;

	/**
	 * 
	 * @param cache    - {@link FileCacheService#getRawObject(String, boolean)}
	 * @param urls     Get array url from {@link Repo}
	 * @param metadata - {@link Metadata}
	 * @return local file or null pointer
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	Path getLocalRawObject(List<String> urls, Metadata metadata, boolean cache)
			throws IOException, NoSuchAlgorithmException;
	/**
	 * @see FileCacheService#getLocalRawObject(List, Metadata, boolean)
	 */
	Path getLocalRawObject(List<String> urls, String urn, boolean cache) throws IOException, NoSuchAlgorithmException;

}
