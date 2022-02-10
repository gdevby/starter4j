package by.gdev.http.download.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import by.gdev.http.download.model.Headers;
import by.gdev.http.download.model.RequestMetadata;
import by.gdev.http.download.service.HttpService;
import lombok.AllArgsConstructor;

/**
 * {@inheritDoc}
 */
@AllArgsConstructor
public class HttpServiceImpl implements HttpService {
	/**
	 *If proxy value is not empty it will be used on connection error
	 */
	private String proxy;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private int maxAttepmts;

	
	 /**
	  * {@inheritDoc}
	  */
	@Override
	public RequestMetadata getRequestByUrlAndSave(String url, Path path) throws IOException {
		RequestMetadata request = null;
		for (int attepmts = 0; attepmts < maxAttepmts; attepmts++) {
			try {
				try {
					request = getResourseByUrl(url, path);
				} catch (IOException e) {
					if (Objects.nonNull(proxy))
						request = getResourseByUrl(proxy + url, path);
					 else 
						throw e;
				}
			} catch (SocketTimeoutException e1) {
				attepmts++;
				if (attepmts == maxAttepmts)
					throw new SocketTimeoutException();
			}
		}
		return request;
	}

	 /**
	  * {@inheritDoc}
	 * @throws IOException 
	  */
		@Override
		public RequestMetadata getMetaByUrl(String url) throws IOException {
			RequestMetadata request = null;
			for (int attepmts = 0; attepmts < maxAttepmts; attepmts++) {
				try {
					request = getMetadata(url);
				} catch (SocketTimeoutException e) {
					attepmts++;
					if (attepmts == maxAttepmts)
						throw new SocketTimeoutException();
				}
			}
			return request;
		}
	
	private RequestMetadata getMetadata(String url) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpResponse response = getResponse(httpUrl);
		if (response.containsHeader(Headers.ETAG.getValue()))
			request.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
		else
			request.setETag(null);
		if (response.containsHeader(Headers.LASTMODIFIED.getValue()))
			request.setLastModified(response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue().replaceAll("\"", ""));
		else
			request.setLastModified(null);
		if(response.containsHeader(Headers.CONTENTLENGTH.getValue()))
			request.setContentLength(response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue());
		else
			request.setContentLength(null);
		return request;
	}
	
	private RequestMetadata getResourseByUrl(String url, Path path) throws IOException, SocketTimeoutException {
		HttpGet httpGet = new HttpGet(url);
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		if (!path.toFile().getParentFile().exists())
			path.toFile().getParentFile().mkdirs();
		Path temp = Paths.get(path.toAbsolutePath().toString() + ".temp");
			try {
				CloseableHttpResponse response = getResponse(httpGet);
				HttpEntity entity = response.getEntity();
				if (response.getStatusLine().getStatusCode() == 404) {
	                EntityUtils.consume(entity);
	                throw new FileNotFoundException(String.valueOf(response.getStatusLine() +" " + url));
	            }		
				if (response.getStatusLine().getStatusCode() > 500) {
	                EntityUtils.consume(entity);
	                throw new IOException(String.valueOf(response.getStatusLine()));
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
				httpGet.abort();
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
			Files.move(Paths.get(temp.toString()), path.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
			return getMetadata(url);
	}
	
	private CloseableHttpResponse getResponse(HttpRequestBase http) throws IOException {
		http.setConfig(requestConfig);
		return httpclient.execute(http);
	}
}