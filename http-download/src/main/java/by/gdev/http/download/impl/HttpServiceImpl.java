package by.gdev.http.download.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.http.download.model.Headers;
import by.gdev.http.download.model.RequestMetadata;
import by.gdev.http.download.service.HttpService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@inheritDoc}
 */
@AllArgsConstructor
@Slf4j
public class HttpServiceImpl implements HttpService {
	/**
	 * If proxy value is not empty it will be used on connection error
	 */
	private String proxy;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private int maxAttepmts;

	private final Map<Path, Lock> fileLocks = new ConcurrentHashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestMetadata getRequestByUrlAndSave(String url, Path path) throws IOException {
		log.debug("do request {}, saved to ", url, path.toAbsolutePath().toString());
		RequestMetadata request = null;
		for (int attepmts = 0; attepmts < maxAttepmts; attepmts++) {
			try {
				request = getResourseByUrl(url, path);
				break;
			} catch (SocketTimeoutException e1) {
				attepmts++;
				if (attepmts == maxAttepmts)
					throw new SocketTimeoutException();
			} catch (IOException e) {
				if (Objects.nonNull(proxy))
					request = getResourseByUrl(proxy + url, path);
				else
					throw e;
			}
		}
		return request;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public RequestMetadata getMetaByUrl(String url) throws IOException {
		RequestMetadata request = null;
		for (int attepmts = 0; attepmts < maxAttepmts; attepmts++) {
			try {
				request = getMetadata(url);
				break;
			} catch (SocketTimeoutException e) {
				attepmts++;
				if (attepmts == maxAttepmts)
					throw new SocketTimeoutException();
			}
		}
		return request;
	}

	@Override
	public String getRequestByUrl(String url) throws IOException {
		return getRequestByUrl(url, null);
	}

	private String getStringByUrl(String url, Map<String, String> headers) throws IOException {
		InputStream in = null;
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(url);
			if (Objects.nonNull(headers)) {
				for (Map.Entry<String, String> e : headers.entrySet())
					httpGet.addHeader(e.getKey(), e.getValue());
			}
			CloseableHttpResponse response = getResponse(httpGet);
			StatusLine st = response.getStatusLine();
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
				in = response.getEntity().getContent();
				return IOUtils.toString(in, StandardCharsets.UTF_8);
			} else {
				throw new IOException(
						String.format("code %s phrase %s %s", st.getStatusCode(), st.getReasonPhrase(), url));
			}
		} finally {
			if (Objects.nonNull(httpGet))
				httpGet.abort();
			IOUtils.closeQuietly(in);
		}
	}

	private RequestMetadata getMetadata(String url) throws IOException {
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpResponse response = getResponse(httpUrl);
		RequestMetadata request = generateRequestMetadata(response);
		return request;
	}

	private RequestMetadata getResourseByUrl(String url, Path path) throws IOException, SocketTimeoutException {
		HttpGet httpGet = new HttpGet(url);
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		if (!path.toFile().getParentFile().exists())
			path.toFile().getParentFile().mkdirs();
		Path temp = Paths.get(path.toAbsolutePath().toString() + ".temp");
		CloseableHttpResponse response;
		Lock lock = fileLocks.computeIfAbsent(temp, key -> new ReentrantLock());
		lock.lock();
		try {
			response = getResponse(httpGet);
			StatusLine st = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
				throw new IOException(String.format("code %s phrase %s", st.getStatusCode(), st.getReasonPhrase()));
			}
			in = new BufferedInputStream(entity.getContent());
			out = new BufferedOutputStream(new FileOutputStream(temp.toFile()));
			byte[] buffer = new byte[65536];
			int curread = in.read(buffer);
			while (curread != -1) {
				out.write(buffer, 0, curread);
				curread = in.read(buffer);
			}
		} finally {
			if (Objects.nonNull(httpGet))
				httpGet.abort();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			lock.unlock();
		}
		Files.move(Paths.get(temp.toString()), path.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
		RequestMetadata requestMetadata = generateRequestMetadata(response);
		return requestMetadata;

	}

	private CloseableHttpResponse getResponse(HttpRequestBase http) throws IOException {
		http.setConfig(requestConfig);
		return httpclient.execute(http);
	}

	private RequestMetadata generateRequestMetadata(CloseableHttpResponse response) {
		RequestMetadata requestMetadata = new RequestMetadata();
		if (response.containsHeader(Headers.ETAG.getValue()))
			requestMetadata.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
		if (response.containsHeader(Headers.LASTMODIFIED.getValue()))
			requestMetadata.setLastModified(
					response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue().replaceAll("\"", ""));
		return requestMetadata;
	}

	@Override
	public String getRequestByUrl(String url, Map<String, String> map) throws IOException {
		SocketTimeoutException ste = null;
		for (int attepmts = 0; attepmts < maxAttepmts; attepmts++) {
			try {
				return getStringByUrl(url, map);
			} catch (SocketTimeoutException e) {
				ste = e;
			}
		}
		throw ste;
	}
}