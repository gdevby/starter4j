import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import by.gdev.config.HttpConfig;
import by.gdev.http.cache.exeption.StatusExeption;
import by.gdev.http.head.cache.impl.DownloaderImpl;
import by.gdev.http.head.cache.impl.FileCacheServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.model.downloader.DownloaderStatusEnum;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.util.model.download.Repo;

public class DownloadTest {
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
		HttpConfig httpConfig = new HttpConfig();
		EventBus eventBus = new EventBus();
		eventBus.register(new ConsoleSubscriber());
		HttpService httpService = new HttpServiceImpl(null, httpConfig.httpClient(), httpConfig.requestConfig(), 3);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
		gsonService = new GsonServiceImpl(gson, fileService);
		downloader = new DownloaderImpl(eventBus, httpConfig.httpClient(), httpConfig.requestConfig());
	}

	@Test
	public void test1() throws FileNotFoundException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException, StatusExeption{
		Repo repo = gsonService.getObject("http://localhost:81/starter-app/1.0/dependencises.json", Repo.class, false);
		DownloaderContainer container = new DownloaderContainer();
		container.setRepo(repo);
		downloader.addContainer(container);
		//download
		downloader.startDownload(true);
//		Thread.sleep(1000);
//		downloader.cancelDownload();
//		Assert.assertEquals(DownloaderStatusEnum.CANCEL, downloader.getStatus());
	}
	
//	@Test
//	public void test2() throws FileNotFoundException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException, StatusExeption {
//		Repo repo = gsonService.getObject("http://localhost:81/starter-app/1.0/dependencises.json", Repo.class, false);
//		DownloaderContainer container = new DownloaderContainer();
//		container.setRepo(repo);
//		downloader.addContainer(container);
//		//download
//		downloader.startDownload(true);
//		Thread.sleep(1000);
//		downloader.cancelDownload();
//		Thread.sleep(1000);
//		downloader.startDownload(true);
//		Assert.assertEquals(DownloaderStatusEnum.IDLE, downloader.getStatus());
//	} 
	
	@Test (expected = StatusExeption.class)
	public void test3() throws FileNotFoundException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException, StatusExeption {
		Repo repo = gsonService.getObject("http://localhost:81/starter-app/1.0/dependencises.json", Repo.class, false);
		DownloaderContainer container = new DownloaderContainer();
		container.setRepo(repo);
		downloader.addContainer(container);
		//download
		downloader.startDownload(true);
		downloader.startDownload(true);
		Assert.assertEquals(DownloaderStatusEnum.IDLE, downloader.getStatus());
	} 
}