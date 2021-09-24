package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import by.gdev.http.head.cache.model.RequestMetadata;
import by.gdev.http.head.cache.service.HttpService;

public class HttpServiceImpl implements HttpService {
	// Удалять есть существует перезаписью
	// Выкинуть Exception и проверить как отработает этот метод
	// Сделать общий метод для извеления методанный этого метода и getMetaByUrl

	@Override
	public RequestMetadata getResourseByUrlAndSave(String url, Path path) throws IOException {
		RequestMetadata request = new RequestMetadata();
		HttpGet httpGet = new HttpGet(url);
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		Path temp = Paths.get(path.toAbsolutePath().toString() + ".temp");
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().build();
			httpGet.setConfig(requestConfig);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			in = new BufferedInputStream(entity.getContent());
			out = new BufferedOutputStream(new FileOutputStream(temp.toFile()));
			byte[] buffer = new byte[65536];
			int curread = in.read(buffer);
			while (curread != -1) {
				out.write(buffer, 0, curread);
				curread = in.read(buffer);
			}
			if (path.toFile().exists())
				Files.delete(path);
			Files.move(Paths.get(temp.toString()), path);
			request.setContentLength(response.getFirstHeader("Content-Length").getValue());
			request.setETag(response.getFirstHeader("ETag").getValue().replaceAll("\"", ""));
			request.setLastModified(response.getFirstHeader("Last-Modified").getValue());
		} catch (IOException e) {
			if (temp.toFile().exists())
				Files.delete(temp);
			else
				temp.toFile().getParentFile().mkdirs();
		} finally {
			httpGet.abort();
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		return request;
	}

	public CloseableHttpResponse getGetResponse(String url) throws IOException {
		HttpGet httpUrl = new HttpGet(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().build();
		httpUrl.setConfig(requestConfig);
		CloseableHttpResponse response = httpclient.execute(httpUrl);
		return response;
	}

	public CloseableHttpResponse getHeadResponse(String url) throws IOException {
		HttpHead httpUrl = new HttpHead(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().build();
		httpUrl.setConfig(requestConfig);
		CloseableHttpResponse response = httpclient.execute(httpUrl);
		return response;
	}

	public RequestMetadata getMetaByUrl(String url, CloseableHttpResponse response) throws IOException {
		RequestMetadata request = new RequestMetadata();
		request.setContentLength(response.getFirstHeader("Content-Length").getValue());
		request.setETag(response.getFirstHeader("ETag").getValue().replaceAll("\"", ""));
		request.setLastModified(response.getFirstHeader("Last-Modified").getValue());
		return request;
	}
}
