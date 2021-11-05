package by.gdev.http.head.cache.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;

import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import lombok.AllArgsConstructor;

/**
 * {@inheritDoc}
 */

@AllArgsConstructor
public class GsonServiceImpl implements GsonService {
	private Gson gson;
	private FileCacheService fileService;

	 /**
	  * {@inheritDoc}
	  */
	
	@Override
	public <T> T getObject(String url, Class<T> class1, boolean cache) throws IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getRawObject(url, cache);
		try (BufferedReader read = new BufferedReader(new FileReader(pathFile.toFile()))) {
			return gson.fromJson(read, class1);
		}
	}
}
