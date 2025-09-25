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
import by.gdev.util.model.InternetServerMap;
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
	public <T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		return getObjectByUrls1(urls, urn, class1, null, cache);
	}

	@Override
	public <T> T getObjectByUrls(List<String> urls, String urn, Type type, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		return getObjectByUrls1(urls, urn, null, type, cache);
	}

	@Override
	public <T> T getLocalObject(List<String> uris, String urn, Class<T> class1)
			throws IOException, NoSuchAlgorithmException {
		return getLocalObject1(uris, urn, class1, null);
	}

	@Override
	public <T> T getLocalObject(List<String> uris, String urn, Type type) throws IOException, NoSuchAlgorithmException {
		return getLocalObject1(uris, urn, null, type);
	}

	private <T> T getLocalObject1(List<String> uris, String urn, Class<T> class1, Type type)
			throws IOException, NoSuchAlgorithmException, FileNotFoundException {
		Path pathFile = fileService.getLocalRawObject(uris, urn);
		if (Objects.isNull(pathFile)) {
			return null;
		}
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
				StandardCharsets.UTF_8)) {
			return Objects.nonNull(class1) ? gson.fromJson(read, class1) : gson.fromJson(read, type);
		}
	}

	protected <T> T getObjectByUrls1(List<String> urls, String urn, Class<T> class1, Type type, boolean cache)
			throws IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getRawObject(urls, urn, cache);
		if (Objects.isNull(pathFile)) {
			return null;
		}
		try (InputStreamReader read = new InputStreamReader(new FileInputStream(pathFile.toFile()),
				StandardCharsets.UTF_8)) {
			return Objects.nonNull(class1) ? gson.fromJson(read, class1) : gson.fromJson(read, type);
		}
	}

	protected <T> T doRequest(List<String> urls, String urn, Class<T> class1, Type type, Map<String, String> headers)
			throws IOException {
		IOException ex = null;
		for (String url : workedServers.getAliveDomainsOrUseAllWithSort(urls)) {
			try {
				String s = httpService.getRequestByUrl(url + urn, headers);
				return Objects.nonNull(class1) ? gson.fromJson(s, class1) : gson.fromJson(s, type);
			} catch (IOException e) {
				ex = e;
			}
		}
		throw ex;
	}

}