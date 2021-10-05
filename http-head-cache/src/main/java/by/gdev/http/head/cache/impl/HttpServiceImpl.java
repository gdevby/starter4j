package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;

	 /**
	  * {@inheritDoc}
	  */
	
	@Override
	public RequestMetadata getResourseByUrlAndSave(String url, Path path) {
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
			request.setContentLength(response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue());
			request.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
			request.setLastModified(response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue());
			
	    } catch (IOException e) {
	    	System.out.println("URL error: " + url);
	    }
		finally {
			httpGet.abort();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		return request;
	}

	 /**
	  * {@inheritDoc}
	  */
	
	public RequestMetadata getMetaByUrl(String url) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpResponse response = getResponse(httpUrl);
		request.setContentLength(response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue());
		request.setETag(response.getFirstHeader(Headers.ETAG.getValue()).getValue().replaceAll("\"", ""));
		request.setLastModified(response.getFirstHeader(Headers.LASTMODIFIED.getValue()).getValue());
		return request;
	}
	
	private CloseableHttpResponse getResponse(HttpRequestBase http) throws IOException {
		http.setConfig(requestConfig);
		return httpclient.execute(http);
	}
}