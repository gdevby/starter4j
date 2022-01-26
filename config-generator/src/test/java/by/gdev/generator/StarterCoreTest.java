package by.gdev.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.eventbus.EventBus;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.handler.Localise;
import by.gdev.http.upload.config.HttpClientConfig;
import by.gdev.http.upload.impl.DownloaderImpl;
import by.gdev.http.upload.impl.FileCacheServiceImpl;
import by.gdev.http.upload.impl.GsonServiceImpl;
import by.gdev.http.upload.impl.HttpServiceImpl;
import by.gdev.http.upload.service.Downloader;
import by.gdev.http.upload.service.FileCacheService;
import by.gdev.http.upload.service.GsonService;
import by.gdev.http.upload.service.HttpService;
import by.gdev.subscruber.ConsoleSubscriber;

public class StarterCoreTest {
	static HttpServer server;
	static GsonService gsonService;
	static Downloader downloader;
	static AppConfigModel acm = AppConfigModel.DEFAULT_APP_CONFIG_MODEL;
	static String testWorkDirectory = "target/out/testContainer/";
	
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
			Path testFolder = Paths.get(testWorkDirectory);
			if (testFolder.toFile().exists()) {
				FileUtils.deleteDirectory(testFolder.toFile());
			}
			testFolder.toFile().mkdirs();
			HttpClientConfig httpConfig = new HttpClientConfig();
			EventBus eventBus = new EventBus();
			ResourceBundle bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
			eventBus.register(new ConsoleSubscriber(bundle));
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
			HttpService httpService = new HttpServiceImpl(null, httpConfig.getInstanceHttpClient(), requestConfig, 3);
			FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
			gsonService = new GsonServiceImpl(gson, fileService);
			downloader = new DownloaderImpl(eventBus, httpConfig.getInstanceHttpClient(), requestConfig);
	}
	
	@AfterClass
	public static void stop() {
		server.stop();
	}
	
	@Test
	public void mainTest() throws Exception {
		String[] configGenerator = {"-name", "test-core", "-version", "0.9", "-url", "http://127.0.0.1:65079/", "-mainClass" , "desktop.starter.app.Main"};
		by.gdev.generator.Main.main(configGenerator);
		String[] starterCoreArg = { "-mainAppConfig",UrlEscapers.urlFragmentEscaper().escape("http://127.0.0.1:65079/test-core/0.9"), 
				"-workDirectory" , testWorkDirectory, "-stop"};
		by.gdev.Main.main(starterCoreArg);
	}
}