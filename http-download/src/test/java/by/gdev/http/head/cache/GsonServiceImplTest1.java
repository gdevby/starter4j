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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;

import com.google.gson.Gson;

import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.head.cache.model.MyTestType;

public class GsonServiceImplTest1 {
	static GsonService gsonService;
	static HttpService httpService;
	static ClientAndServer mockServer;

	@BeforeClass
	public static void init() throws IOException {
		Path testFolder = Paths.get("target/test_folder");
		if (testFolder.toFile().exists()) {
			FileUtils.deleteDirectory(testFolder.toFile());
		}
		testFolder.toFile().mkdirs();
		mockServer = ClientAndServer.startClientAndServer("127.0.0.1", 12346);
		ConfigurationProperties.disableSystemOut(true);
		mockServer.when(request().withMethod("GET").withPath("/validate"))
				.respond(HttpResponse.response().withStatusCode(201)
						.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"),
								new Header("Cache-Control", "public, max-age=86400"))
						.withBody("{ message: 'incorrect username and password combination' }")
						.withDelay(TimeUnit.SECONDS, 10));
		initialization(testFolder);

	}

	private static void initialization(Path testFolder) {
		Gson gson = new Gson();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();
		HttpService httpService = new HttpServiceImpl(null, HttpClientConfig.getInstanceHttpClient(), requestConfig, 3);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder,
				600000);
		gsonService = new GsonServiceImpl(gson, fileService, httpService);
	}

	@After
	public void stop() {
		mockServer.stop();
	}

	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 = gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, false);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}

	@Test
	public void test2() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 = gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, true);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}

	@Test(expected = FileNotFoundException.class)
	public void test3() throws NoSuchAlgorithmException, IOException {
		gsonService.getObject("https://gdev.by/repo/testnotwxisrt.json", MyTestType.class, true);
	}

	@Test(expected = UnknownHostException.class)
	public void test4() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		gsonService.getObject("https://domennotexistgdev.by/repo/testnotwxisrt.json", MyTestType.class, false);
	}

	@Test(expected = HttpHostConnectException.class)
	public void test5Timeout() throws NoSuchAlgorithmException, IOException {
		gsonService.getObject("http://127.0.0.1:12346/repo/test78.json", MyTestType.class, true);
	}

	@Test(expected = HttpHostConnectException.class)
	public void test6Timeout() throws IOException, NoSuchAlgorithmException {
		gsonService.getObject("http://127.0.0.1:12346/repo/test77.json", MyTestType.class, false);
	}

	@Test
	public void test7() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		MyTestType test = gsonService.getObjectByUrls(
				Arrays.asList("https://domennotexistgdev.by/", "https://gdev.by/"), "repo/test.json", MyTestType.class,
				false);
		System.out.println(test);
	}

	@Test
	public void test8() throws IOException {
		MyTestType test = gsonService.getObjectWithoutSaving("https://gdev.by/repo/test.json", MyTestType.class);
		System.out.println(test);
	}

}