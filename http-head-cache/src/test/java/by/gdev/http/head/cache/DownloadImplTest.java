package by.gdev.http.head.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import by.gdev.http.head.cache.impl.DownloaderImpl;
import by.gdev.http.head.cache.impl.FileCacheServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import by.gdev.util.model.download.Repo;

public class DownloadImplTest {
	static GsonService gsonService;
	static DownloaderImpl downloader;

	@BeforeClass
	public static void init() throws IOException {
		Path testFolder = Paths.get("target/test_folder");
		if (testFolder.toFile().exists()) {
			FileUtils.deleteDirectory(testFolder.toFile());
		}
		testFolder.toFile().mkdirs();

		Gson gson = new Gson();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(5);
		cm.setMaxTotal(20);
		CloseableHttpClient builder = HttpClients.custom().setKeepAliveStrategy((response, context) -> {
			Args.notNull(response, "HTTP response");
			final HeaderElementIterator it = new BasicHeaderElementIterator(
					response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			if (it.hasNext()) {
				return 5000L;
			}
			return -1;
		}).setConnectionManager(cm).evictIdleConnections(10, TimeUnit.SECONDS).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();

		HttpService httpService = new HttpServiceImpl(null, builder, requestConfig, 3);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
		gsonService = new GsonServiceImpl(gson, fileService);
		downloader = new DownloaderImpl("/home/aleksandr/Desktop/qwert/www/");

	}

	@Test
	public void test() throws Exception{
//		Repo repo = gsonService.getObject("http://localhost:81/old/test_download.json", Repo.class, false);
		Repo repo = gsonService.getObject("http://localhost:81/starter-app/1.0/dependencises.json", Repo.class, false);
		List<Repo> list = new ArrayList<Repo>();
		list.add(repo);
		DownloaderContainer container = new DownloaderContainer();
		for (Repo r : list) {
			container.setRepo(r);
			downloader.addContainer(container);
		}
		downloader.startDownload(true);
		Thread.sleep(1000);
		downloader.cancelDownload();
		Thread.sleep(1000);
		downloader.startDownload(true);
		System.out.println(downloader.getStatus());
//		Assert.assertEquals(DownloaderStatusEnum.IDLE, downloader.getStatus());
	}
	
	
}
