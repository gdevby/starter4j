package by.gdev.core;
import static org.mockserver.model.HttpRequest.request;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import by.gdev.handler.Localise;
import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.exeption.StatusExeption;
import by.gdev.http.download.handler.AccesHandler;
import by.gdev.http.download.impl.DownloaderImpl;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.upload.download.downloader.DownloaderContainer;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;

public class DownloadTest {
	static GsonService gsonService;
	static DownloaderImpl downloader;
	static ClientAndServer mockServer;

	@BeforeClass
	public static void init() throws IOException {
		Gson gson = new Gson();
		Path testFolder = Paths.get("target/test_folder");
		if (testFolder.toFile().exists()) {
			FileUtils.deleteDirectory(testFolder.toFile());
		}
		testFolder.toFile().mkdirs();
		Repo repo = new FileMapperService(gson, StandardCharsets.UTF_8, "").read("src/test/resources/dependencies.json", Repo.class);
		String str = gson.toJson(repo, Repo.class);
		mockServer = ClientAndServer.startClientAndServer(34631);
		ConfigurationProperties.disableSystemOut(true);
		mockServer.when(request()
	              .withMethod("GET")
	              .withPath("/dependencises.json"))
		.respond(HttpResponse.response()
            .withStatusCode(200)
            .withHeaders(
              new Header("Content-Type", "application/json; charset=utf-8"),
              new Header("Cache-Control", "public, max-age=86400"))
            .withBody(str)
            .withDelay(TimeUnit.SECONDS,2));
		
		mockServer.when(request()
	              .withMethod("HEAD")
	              .withPath("/dependencises.json"))
		.respond(HttpResponse.response()
          .withStatusCode(200)
          .withHeaders(
            new Header("Content-Type", "application/json; charset=utf-8"),
            new Header("Cache-Control", "public, max-age=86400"))
          .withDelay(TimeUnit.SECONDS,1));
		initialization(testFolder, gson);
	}

	private static void initialization(Path testFolder, Gson gson) {
		HttpClientConfig httpConfig = new HttpClientConfig();
		EventBus eventBus = new EventBus();
		ResourceBundle bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
		eventBus.register(new ConsoleSubscriber(bundle, null, null));
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		HttpService httpService = new HttpServiceImpl(null, httpConfig.getInstanceHttpClient(), requestConfig, 3);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
		gsonService = new GsonServiceImpl(gson, fileService);
		downloader = new DownloaderImpl(eventBus, httpConfig.getInstanceHttpClient(), requestConfig);
	}
	
	
	
	
	@AfterClass
	public static void stop() {
		mockServer.stop();
	}

	
	@Test
	public void test1() throws FileNotFoundException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException, StatusExeption {
		Repo repo = gsonService.getObject("http://127.0.0.1:34631/dependencises.json", Repo.class, false);
		AccesHandler accesHandler = new AccesHandler();
		DownloaderContainer container = new DownloaderContainer();
		container.setDestinationRepositories("target/test_folder/");
		container.setRepo(repo);
		container.setHandlers(Arrays.asList(accesHandler));
		downloader.addContainer(container);
		downloader.startDownload(true);
		Thread.sleep(1000);
		downloader.cancelDownload();
		Assert.assertEquals(DownloaderStatusEnum.CANCEL, downloader.getStatus());
	} 
	
	@Test (expected = StatusExeption.class)
	public void test2() throws FileNotFoundException, NoSuchAlgorithmException, IOException, InterruptedException, ExecutionException, StatusExeption {
		Repo repo = gsonService.getObject("http://127.0.0.1:34631/dependencises.json", Repo.class, false);
		AccesHandler accesHandler = new AccesHandler();
		DownloaderContainer container = new DownloaderContainer();
		container.setDestinationRepositories("target/test_folder/containerTest/");
		container.setRepo(repo);
		container.setHandlers(Arrays.asList(accesHandler));
		downloader.addContainer(container);
		downloader.startDownload(true);
		Thread.sleep(1000);
		downloader.startDownload(true);
	} 
}