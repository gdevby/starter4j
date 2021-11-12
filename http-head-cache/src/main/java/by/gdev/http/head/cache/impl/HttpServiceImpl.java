package by.gdev.http.head.cache.impl;

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

import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.RequestMetadata;
import by.gdev.http.head.cache.service.HttpService;
import lombok.AllArgsConstructor;

/**
 * {@inheritDoc}
 */
@AllArgsConstructor
public class HttpServiceImpl implements HttpService {
	private String proxy;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private int maxAttepmts;

	
	 /**
	  * {@inheritDoc}
	  */
	
	@Override
	public RequestMetadata getResourseByUrlAndSave(String url, Path path) throws IOException {
		for (int attepmts = 1; attepmts < maxAttepmts; attepmts++) {
			try {
				try {
					return getResourseByUrl(url, path);
				} catch (IOException e) {
					if (Objects.nonNull(proxy)) {
						return getResourseByUrl(proxy + url, path);
					} else {
						throw new SocketTimeoutException();
					}
				}
			} catch (SocketTimeoutException e1) {
				if (attepmts == maxAttepmts)
					throw new SocketTimeoutException();
			}
		}
		return null;
	}

	 /**
	  * {@inheritDoc}
	 * @throws IOException 
	  */
		@Override
		public RequestMetadata getMetaByUrl(String url) throws IOException {
			for (int attepmts = 1; attepmts < maxAttepmts; attepmts++) {
				try {
					return test123(url);
				} catch (SocketTimeoutException e) {
					attepmts++;
					if (attepmts == maxAttepmts)
						throw new SocketTimeoutException();
				}
			}

			return null;
	// TODO: ???
			
//			int attepmts = 1;
//			while (attepmts < maxAttepmts) {
//				try {
//					return test123(url);
//				} catch (SocketTimeoutException e) {
//					attepmts++;
//					if (attepmts == maxAttepmts)
//						throw new SocketTimeoutException();
//				}
//			}
//			return null;
		}
		// TODO ????
	private RequestMetadata test123(String url) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpResponse response = getResponse(httpUrl);
		try {
			request.setContentLength(response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue());
			request.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
			request.setLastModified(response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue());
		}catch (NullPointerException e) {
			request.setContentLength("");
			request.setETag("");
			request.setLastModified("");
		}
		return request;
	}
	//TODO move method to desktop util and allow to add own link
	@Override
	public void init() {
		try {
			HttpHead http = new HttpHead("http://www.google.com");
			getResponse(http);
		} catch (IOException e) {
			maxAttepmts = 1;
	    }
	}
	
	private RequestMetadata getResourseByUrl(String url, Path path) throws IOException, SocketTimeoutException {
		RequestMetadata request = new RequestMetadata();
		HttpGet httpGet = new HttpGet(url);
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		Path temp = Paths.get(path.toAbsolutePath().toString() + ".temp");
			try {
				CloseableHttpResponse response = getResponse(httpGet);
				HttpEntity entity = response.getEntity();
				if (response.getStatusLine().getStatusCode() == 404) {
	                EntityUtils.consume(entity);
	                throw new FileNotFoundException(String.valueOf(response.getStatusLine()));
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
				Files.move(Paths.get(temp.toString()), path, StandardCopyOption.REPLACE_EXISTING);
				
				try {
					request.setContentLength(response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue());
					request.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
					request.setLastModified(response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue());
				}catch (NullPointerException e) {
					request.setContentLength("");
					request.setETag("");
					request.setLastModified("");
				}
			} finally {
				httpGet.abort();
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
			return request;
	}
	
	private CloseableHttpResponse getResponse(HttpRequestBase http) throws IOException {
		http.setConfig(requestConfig);
		return httpclient.execute(http);
	}
}