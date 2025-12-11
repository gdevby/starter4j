package by.gdev.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.http.download.service.Downloader;
import by.gdev.http.download.service.GsonService;

public class StarterCoreTest {
	static HttpServer server;
	static GsonService gsonService;
	static Downloader downloader;
	static AppConfigModel acm = AppConfigModel.DEFAULT_APP_CONFIG_MODEL;
	static String testWorkDirectory = "target/out/testContainer/";
	static File jre = new File("target/out/test-core/jres_default");

	@BeforeClass
	public static void init() throws IOException {
		SocketConfig config = SocketConfig.copy(SocketConfig.DEFAULT).setSoReuseAddress(true).build();
		server = ServerBootstrap.bootstrap().setListenerPort(65079).setSocketConfig(config)
				.registerHandler("*", new HttpRequestHandlerServer(acm)).create();
		server.start();

		if (jre.exists()) {
			FileUtils.deleteDirectory(jre);
		}

		Path testFolder = Paths.get(testWorkDirectory).toAbsolutePath();
		if (testFolder.toFile().exists()) {
			FileUtils.deleteDirectory(testFolder.toFile());
		}
		testFolder.toFile().mkdirs();
	}

	@AfterClass
	public static void stop() {
		server.stop();
	}

	@Test
	public void mainTest() throws Exception {
		String[] configGenerator = { "-name", "test-core", "-version", "0.9", "-url", "http://127.0.0.1:65079/",
				"-mainClass", "desktop.starter.app.Main" };
		by.gdev.generator.Main.main(configGenerator);
		FileUtils.copyDirectory(new File(acm.getJavaFolder()), jre);
		String[] starterCoreArg = { "-uriAppConfig", "http://127.0.0.1:65079/test-core/", "-version", "0.9",
				"-workDirectory", Paths.get(testWorkDirectory).toAbsolutePath().toString().concat("/"), "-stop" };
		by.gdev.Main.main(starterCoreArg);
	}
}