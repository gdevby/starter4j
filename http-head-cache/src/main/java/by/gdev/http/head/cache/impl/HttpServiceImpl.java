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

import by.gdev.http.head.cache.model.RequestMetadata;
import by.gdev.http.head.cache.service.HttpService;
//todo description from interface
public class HttpServiceImpl implements HttpService {
	//todo private 
	CloseableHttpClient httpclient;
	RequestConfig requestConfig;
	//todo remove russian
	// Удалять есть существует перезаписью
	// Выкинуть Exception и проверить как отработает этот метод
	// Сделать общий метод для извеления методанный этого метода и getMetaByUrl

	public HttpServiceImpl(CloseableHttpClient httpclient,RequestConfig requestConfig) {
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
	}
	
	@Override
	public RequestMetadata getResourseByUrlAndSave(String url, Path path) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpGet httpGet = new HttpGet(url);
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		Path temp = Paths.get(path.toAbsolutePath().toString() + ".temp");
		try {
			CloseableHttpResponse response = getResponse(httpGet);
			HttpEntity entity = response.getEntity();
			//== 404 and we throw , FileNotFoundException but if we have any other error > 400 we shuld throw IOException
			if (response.getStatusLine().getStatusCode() != 200) {
                EntityUtils.consume(entity);
                throw new FileNotFoundException(String.valueOf(response.getStatusLine().getStatusCode()));
            }
			in = new BufferedInputStream(entity.getContent());
			out = new BufferedOutputStream(new FileOutputStream(temp.toFile()));
			byte[] buffer = new byte[65536];
			int curread = in.read(buffer);
			while (curread != -1) {
				out.write(buffer, 0, curread);
				curread = in.read(buffer);
			}
			//used REPLACE_EXISTING
			if (path.toFile().exists())
				Files.delete(path);
			Files.move(Paths.get(temp.toString()), path);
			request.setContentLength(response.getFirstHeader("Content-Length").getValue());
			request.setETag(response.getFirstHeader("ETag").getValue().replaceAll("\"", ""));
			request.setLastModified(response.getFirstHeader("Last-Modified").getValue());
			
			//ioException can't be thrown
		} catch (IOException e) {
			if (temp.toFile().exists())
				Files.delete(temp);
			//todo ???
			else
				temp.toFile().getParentFile().mkdirs();
		} finally {
			httpGet.abort();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		return request;
	}
	//todo position

	private CloseableHttpResponse getResponse(HttpRequestBase http) throws IOException {
		http.setConfig(requestConfig);
		return httpclient.execute(http);
	}

	public RequestMetadata getMetaByUrl(String url) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpResponse response = getResponse(httpUrl);
		//create enum with string content ,etag, last....
		request.setContentLength(response.getFirstHeader("Content-Length").getValue());
		request.setETag(response.getFirstHeader("ETag").getValue().replaceAll("\"", ""));
		request.setLastModified(response.getFirstHeader("Last-Modified").getValue());
		return request;
	}
}