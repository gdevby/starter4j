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
import java.util.Objects;

import com.google.gson.Gson;

import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.util.InternetServerMap;
import lombok.AllArgsConstructor;

/**
 * {@inheritDoc}
 */
@AllArgsConstructor
public class GsonServiceImpl implements GsonService {
	private Gson gson;
	private FileCacheService fileService;
	private HttpService httpService;
	private InternetServerMap workedServers;

	/**
	 * {@inheritDoc}
	 */

	@Override
	public <T> T getObjectWithoutSaving(List<String> urls, String urn, Class<T> class1) throws IOException {
		return getObjectWithoutSaving(urls, urn, class1, null);
	}

	@Override
	public <T> T getObjectWithoutSaving(List<String> urls, String urn, Type type) throws IOException {
		return getObjectWithoutSaving(urls, urn, type, null);
	}

	@Override
	public <T> T getObjectWithoutSaving(List<String> urls, String urn, Class<T> class1, Map<String, String> headers)
			throws IOException {
		return doRequest(urls, urn, class1, null, headers);

	}

	@Override
	public <T> T getObjectWithoutSaving(List<String> urls, String urn, Type type, Map<String, String> headers)
			throws IOException {
		return doRequest(urls, urn, null, type, headers);
	}

	@Override
	public <T> T getLocalObject(List<String> uris, String urn, Class<T> class1)
			throws IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getLocalRawObject(uris, urn);
		if (Objects.isNull(pathFile))
			return null;
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
				StandardCharsets.UTF_8)) {
			return gson.fromJson(read, class1);
		}
	}

	@Override
	public <T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getRawObject(urls, urn, cache);
		if (Objects.isNull(pathFile))
			return null;
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
				StandardCharsets.UTF_8)) {
			return gson.fromJson(read, class1);
		}
	}

	@Override
	public <T> T getLocalObject(String uri, Class<T> class1) throws IOException, NoSuchAlgorithmException {
		return null;
	}

	protected <T> T doRequest(List<String> urls, String urn, Class<T> class1, Type type, Map<String, String> headers)
			throws IOException {
		IOException ex = null;
		for (String url : urls) {
			try {
				if (workedServers.isSkippedURL(url))
					continue;
				String s = httpService.getRequestByUrl(url + urn, headers);
				return Objects.nonNull(class1) ? gson.fromJson(s, class1) : gson.fromJson(s, type);
			} catch (IOException e) {
				ex = e;
			}
		}
		throw ex;
	}

}