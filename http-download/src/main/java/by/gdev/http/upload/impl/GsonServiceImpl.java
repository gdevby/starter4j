package by.gdev.http.upload.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.google.gson.Gson;

import by.gdev.http.upload.service.FileCacheService;
import by.gdev.http.upload.service.GsonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@inheritDoc}
 */
@Slf4j
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
		//TODO where is encoding?
		try (BufferedReader read = new BufferedReader(new FileReader(pathFile.toFile()))) {
			return gson.fromJson(read, class1);
		}
	}
	
	 /**
	  * {@inheritDoc}
	  */
		@Override
		public <T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
				throws FileNotFoundException, IOException, NoSuchAlgorithmException {
			Path pathFile = null;
			for (String url : urls) {
				try {
					pathFile = fileService.getRawObject(url + urn, cache);
					try (BufferedReader read = new BufferedReader(new FileReader(pathFile.toFile()))) {
						return gson.fromJson(read, class1);
					}
				} catch (IOException e) {
					log.error("Error = "+e.getMessage());
				}
			}
			//TODO why runtime?
			throw new RuntimeException("RuntimeException in getObjectByUrls");
		}
}