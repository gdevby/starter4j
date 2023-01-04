package by.gdev.http.download.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
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
	private HttpService httpService;

	/**
	 * {@inheritDoc}
	 */

	@Override
	public <T> T getObject(String url, Class<T> class1, boolean cache) throws IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getRawObject(url, cache);
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
				StandardCharsets.UTF_8)) {
			return gson.fromJson(read, class1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		T returnValue = null;
		for (String url : urls) {
			try {
				Path pathFile = fileService.getRawObject(url + urn, cache);
				try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
						StandardCharsets.UTF_8)) {
					returnValue = gson.fromJson(read, class1);
				}
			} catch (IOException e) {
				log.error("Error = " + e.getMessage());
			}
		}
		return returnValue;
	}

	@Override
	public <T> T getObjectWithoutSaving(String url, Class<T> class1) throws IOException {
		return getObjectWithoutSaving(url, class1, null);
	}

	@Override
	public <T> T getObjectWithoutSaving(String url, Type type) throws IOException {
		return getObjectWithoutSaving(url, type, null);
	}

	@Override
	public <T> T getObjectWithoutSaving(String url, Class<T> class1, Map<String, String> headers) throws IOException {
		return gson.fromJson(httpService.getRequestByUrl(url, headers), class1);
	}

	@Override
	public <T> T getObjectWithoutSaving(String url, Type type, Map<String, String> headers) throws IOException {
		return gson.fromJson(httpService.getRequestByUrl(url, headers), type);
	}
}