package by.gdev.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.http.head.cache.config.HttpConfig;
import by.gdev.http.head.cache.impl.DownloaderImpl;
import by.gdev.http.head.cache.impl.FileCacheServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.service.Downloader;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import by.gdev.subscruber.ConsoleSubscriber;

public class StarterCoreTest {
	static HttpServer server;
	static GsonService gsonService;
	static Downloader downloader;
	static ClientAndServer mockServer;
	static AppConfigModel acm = AppConfigModel.DEFAULT_APP_CONFIG_MODEL;
	
	@BeforeClass
	public static void init() throws IOException {
		 SocketConfig config = SocketConfig.copy(SocketConfig.DEFAULT).setSoReuseAddress(true).build();
		 server = ServerBootstrap.bootstrap()
				 .setListenerPort(65079)
				 .setSocketConfig(config)
				  .registerHandler("*", new HttpRequestHandlerServer(acm))
				 .create();
		 server.start();
		 
		 
		 	Gson gson = new Gson();
			Path testFolder = Paths.get("target/test_folder");
			if (testFolder.toFile().exists()) {
				FileUtils.deleteDirectory(testFolder.toFile());
			}
			testFolder.toFile().mkdirs();
			HttpConfig httpConfig = new HttpConfig();
			EventBus eventBus = new EventBus();
			eventBus.register(new ConsoleSubscriber());
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
			HttpService httpService = new HttpServiceImpl(null, httpConfig.httpClient(), requestConfig, 3);
			FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
			gsonService = new GsonServiceImpl(gson, fileService);
			downloader = new DownloaderImpl(eventBus, httpConfig.httpClient(), requestConfig);
	}
	
	@AfterClass
	public static void stop() {
		server.stop();
	}
	
	@Test
	public void mainTest() throws Exception {
		String[] configGenerator = {"-name", "test-starter-app", "-version", "0.9", "-url", "http://127.0.0.1:65079/", "-mainClass" , "desktop.starter.app.Main"};
		by.gdev.generator.Main.main(configGenerator);
		String[] starterCoreArg = { "-mainAppConfig","http://127.0.0.1:65079/test-starter-app/0.9"};
		by.gdev.Main.main(starterCoreArg);
	}
}