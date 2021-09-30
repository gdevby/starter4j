package by.gdev.http.head.cache.service;

import java.io.IOException;
import java.nio.file.Path;

import by.gdev.http.head.cache.model.RequestMetadata;

public interface HttpService {
	/**
	 * 
	 * @param url
	 * @param path
	 * @return
	 * @throws IOException
	 */
	RequestMetadata getResourseByUrlAndSave(String url, Path path) throws IOException;
	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	RequestMetadata getMetaByUrl(String url) throws IOException;
}
