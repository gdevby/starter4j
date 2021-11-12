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
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpResponse;

import com.google.gson.Gson;

import by.gdev.http.head.cache.impl.FileCacheServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.MyTestType;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		mockServer.when( request()
	              .withMethod("POST")
	              .withPath("/validate"))
	                .respond(
	                		HttpResponse.response()
	                    .withStatusCode(401)
	                    .withHeaders(
	                      new Header("Content-Type", "application/json; charset=utf-8"),
	                      new Header("Cache-Control", "public, max-age=86400"))
	                    .withBody("{ message: 'incorrect username and password combination' }")
	                    .withDelay(TimeUnit.SECONDS,100));
		Gson gson = new Gson();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setDefaultMaxPerRoute(5);
		cm.setMaxTotal(20);
		CloseableHttpClient builder = HttpClients.custom().setKeepAliveStrategy((response, context) -> {
			Args.notNull(response, "HTTP response");
			final HeaderElementIterator it = new BasicHeaderElementIterator(
					response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			if (it.hasNext()) {
				log.info("used keep alive 5000");
				return 5000L;
			}
			return -1;
		}).setConnectionManager(cm).evictIdleConnections(10, TimeUnit.SECONDS).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();

		
		
		
		HttpService httpService = new HttpServiceImpl(null ,builder, requestConfig, 3);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 600000);
		gsonService = new GsonServiceImpl(gson, fileService);

	}

	@After
	public void stop() {
		mockServer.stop();
	}

	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 =  gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, false);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}
	
	@Test
	public void test2() throws NoSuchAlgorithmException, IOException {
		MyTestType type1 =  gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, true);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}
	
	@Test(expected = FileNotFoundException.class)
	public void test3() throws NoSuchAlgorithmException, IOException  {
		gsonService.getObject("https://gdev.by/repo/testnotwxisrt.json", MyTestType.class, true);
	}
	
	@Test(expected = UnknownHostException.class)
	public void test4() throws FileNotFoundException, NoSuchAlgorithmException, IOException  {
		gsonService.getObject("https://domennotexistgdev.by/repo/testnotwxisrt.json", MyTestType.class, false);
	}
	
	@Test (expected = HttpHostConnectException.class) 
	public void test5Timeout() throws NoSuchAlgorithmException, IOException {
		gsonService.getObject("http://127.0.0.1:12346/repo/test78.json", MyTestType.class, true);
	}

	@Test(expected = HttpHostConnectException.class)
	public void test6Timeout() throws IOException, NoSuchAlgorithmException {
		gsonService.getObject("http://127.0.0.1:12346/repo/test77.json", MyTestType.class, false);
	}
}