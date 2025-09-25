package by.gdev.http.head.cache;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.head.cache.model.MyTestType;
import by.gdev.util.model.InternetServerMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GsonServiceImplTest1 {
	static GsonService gsonService;
	static HttpService httpService;
	static ClientAndServer mockServer;
	public static String host = "http://127.0.0.1";
	public static Integer port = 34631;
	public static String url = host + ":" + port + "/";

	@BeforeClass
	public static void init() throws IOException {
		Path testFolder = Paths.get("target/test_folder");
		if (testFolder.toFile().exists()) {
			FileUtils.deleteDirectory(testFolder.toFile());
		}
		testFolder.toFile().mkdirs();
		mockServer = ClientAndServer.startClientAndServer(port);
		ConfigurationProperties.disableSystemOut(true);
		mockServer.when(request().withMethod("GET").withPath("/validate"))
				.respond(HttpResponse.response().withStatusCode(201)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
								new Header("Cache-Control", "public, max-age=86400"))
						.withBody("{ message: 'incorrect username and password combination' }")
						.withDelay(TimeUnit.SECONDS, 10));
		mockServer.when(request().withMethod("GET").withPath("/repo/test.json"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
								new Header("Cache-Control", "public, max-age=86400"))
						.withBody("{ \"s\": \"string\" }"));
		initialization(testFolder);

	}

	private static void initialization(Path testFolder) {
		Gson gson = new Gson();
		HttpService httpService = new HttpServiceImpl(null, HttpClientConfig.getInstanceHttpClient(),
				new InternetServerMap());
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder,
				600000, new InternetServerMap());
		gsonService = new GsonServiceImpl(gson, fileService, httpService, new InternetServerMap());
	}

	@AfterClass
	public static void stop() {
		log.info("stopped server");
		mockServer.stop();
	}

	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 = gsonService.getObjectByUrls(Lists.newArrayList(url), "repo/test.json", MyTestType.class,
				false);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}

	@Test
	public void test2() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 = gsonService.getObjectByUrls(Lists.newArrayList(url), "repo/test.json", MyTestType.class,
				true);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}

	@Test(expected = IOException.class)
	public void test3() throws NoSuchAlgorithmException, IOException {
		gsonService.getObjectByUrls(Lists.newArrayList(host), "repo/testnotwxisrt.json", MyTestType.class, true);
	}

	@Test(expected = UnknownHostException.class)
	public void test4() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		gsonService.getObjectByUrls(Lists.newArrayList("https://domennotexistgdev.by"), "repo/testnotwxisrt.json",
				MyTestType.class, false);
	}

	@Test(expected = HttpHostConnectException.class)
	public void test5Timeout() throws NoSuchAlgorithmException, IOException {
		gsonService.getObjectByUrls(Lists.newArrayList("http://127.0.0.1:12346"), "/repo/test78.json", MyTestType.class,
				true);
	}

	@Test
	public void test7() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		MyTestType test = gsonService.getObjectByUrls(Arrays.asList("https://domennotexistgdev.by/", url),
				"repo/test.json", MyTestType.class, false);
		System.out.println(test);
	}

	@Test
	public void test8() throws IOException {
		gsonService.getObjectWithoutSaving(Lists.newArrayList(url), "repo/test.json", MyTestType.class);
	}

}