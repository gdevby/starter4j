package by.gdev.http.head.cache.service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public interface FileService {
	/**
	 * 
	 * @param url
	 * @param cache
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	Path getRawObject(String url, boolean cache) throws IOException, NoSuchAlgorithmException;
}
