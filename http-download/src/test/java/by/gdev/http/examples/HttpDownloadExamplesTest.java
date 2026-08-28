package by.gdev.http.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.util.Timeout;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.exeption.StatusExeption;
import by.gdev.http.download.handler.PostHandlerImpl;
import by.gdev.http.download.impl.DownloaderImpl;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.Downloader;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.head.cache.model.MyTestType;
import by.gdev.http.upload.download.downloader.DownloaderContainer;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.InternetServerMap;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;

/**
 * Наглядные примеры работы http-download. Каждый тест соответствует сценарию из README.
 */
@Ignore
public class HttpDownloadExamplesTest {

	private static final int PORT = 34632;
	private static final String BASE_URL = "http://127.0.0.1:" + PORT + "/";
	private static final String FILE_PATH = "/examples/demo.bin";
	private static final byte[] FILE_CONTENT = "example-download-content".getBytes(StandardCharsets.UTF_8);

	static ClientAndServer mockServer;
	static CloseableHttpClient client;
	static Gson gson;
	static Path workDir;

	GsonService gsonService;

	@BeforeClass
	public static void startServer() throws IOException {
		workDir = Paths.get("target/examples_workdir");
		mockServer = ClientAndServer.startClientAndServer(PORT);
		ConfigurationProperties.disableSystemOut(true);

		mockServer.when(request().withMethod("GET").withPath(FILE_PATH))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody(new String(FILE_CONTENT, StandardCharsets.UTF_8)));

		client = HttpClientConfig.getInstanceHttpClient();
		gson = new Gson();
	}

	@Before
	public void resetWorkDir() throws IOException {
		FileUtils.deleteDirectory(workDir.toFile());
		workDir.toFile().mkdirs();
		HttpService httpService = new HttpServiceImpl(null, client, new InternetServerMap());
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, StandardCharsets.UTF_8,
				workDir.resolve("cache"), 600_000, new InternetServerMap());
		gsonService = new GsonServiceImpl(gson, fileService, httpService, new InternetServerMap());
	}

	@AfterClass
	public static void stopServer() {
		if (mockServer != null) {
			mockServer.stop();
		}
	}

	private void stubJson(String path, String body, String eTag) {
		mockServer.when(request().withMethod("GET").withPath(path))
				.respond(HttpResponse.response().withStatusCode(200)
						.withHeader("Content-Type", "application/json; charset=utf-8")
						.withHeader("ETag", eTag)
						.withBody(body));
		mockServer.when(request().withMethod("HEAD").withPath(path))
				.respond(HttpResponse.response().withStatusCode(200).withHeader("ETag", eTag));
	}

	/**
	 * Режим A кэширования: при повторном запросе лаунчер спрашивает сервер (HEAD), а не качает файл заново.
	 */
	@Test
	public void example_cacheModeA_reusesFileAfterHeadCheck() throws IOException {
		String jsonPath = "/examples/config-mode-a.json";
		stubJson(jsonPath, "{ \"s\": \"cached-value\" }", "\"v1\"");

		MyTestType first = gsonService.getObjectByUrls(Collections.singletonList(BASE_URL),
				"examples/config-mode-a.json", MyTestType.class, false);
		MyTestType second = gsonService.getObjectByUrls(Collections.singletonList(BASE_URL),
				"examples/config-mode-a.json", MyTestType.class, false);

		assertEquals("cached-value", first.getS());
		assertEquals(first, second);
		assertTrue(workDir.resolve("cache").toFile().exists());
	}

	/**
	 * Режим B кэширования: при повторном запросе сервер не вызывается, если локальная копия цела.
	 */
	@Test
	public void example_cacheModeB_usesLocalCopyWithoutServer() throws IOException {
		String jsonPath = "/examples/config-mode-b.json";
		mockServer.when(request().withMethod("GET").withPath(jsonPath))
				.respond(HttpResponse.response().withStatusCode(200)
						.withHeader("Content-Type", "application/json; charset=utf-8")
						.withBody("{ \"s\": \"local-only\" }"));

		MyTestType first = gsonService.getObjectByUrls(Collections.singletonList(BASE_URL),
				"examples/config-mode-b.json", MyTestType.class, true);
		MyTestType second = gsonService.getObjectByUrls(Collections.singletonList(BASE_URL),
				"examples/config-mode-b.json", MyTestType.class, true);

		assertEquals("local-only", first.getS());
		assertEquals(first, second);
	}

	/**
	 * Зеркала: если первый адрес недоступен, запрос уходит на второй.
	 */
	@Test
	public void example_mirrorFailover_usesWorkingRepository() throws IOException {
		String jsonPath = "/examples/config-mirror.json";
		stubJson(jsonPath, "{ \"s\": \"from-second-mirror\" }", "\"v1\"");

		MyTestType result = gsonService.getObjectByUrls(
				Arrays.asList("https://mirror-unreachable.example/", BASE_URL), "examples/config-mirror.json",
				MyTestType.class, false);
		assertEquals("from-second-mirror", result.getS());
	}

	/**
	 * Загрузка файла по списку из конфига с проверкой SHA-1 после скачивания.
	 */
	@Test
	public void example_downloadFile_validatesChecksumAfterDownload()
			throws IOException, InterruptedException, ExecutionException, StatusExeption {
		downloadDemoFile(Collections.singletonList(BASE_URL), "demo.bin", workDir.resolve("downloads").toString() + File.separator);
	}

	/**
	 * Загрузка файла: первое зеркало недоступно, файл получен со второго.
	 */
	@Test
	public void example_downloadFile_usesSecondMirrorWhenFirstFails()
			throws IOException, InterruptedException, ExecutionException, StatusExeption {
		downloadDemoFile(Arrays.asList("https://mirror-unreachable.example/", BASE_URL), "demo-mirror.bin",
				workDir.resolve("downloads-mirror").toString() + File.separator);
	}

	private void downloadDemoFile(java.util.List<String> repositories, String localFileName, String destination)
			throws IOException, InterruptedException, ExecutionException, StatusExeption {
		String sha1 = DesktopUtil.getChecksum(FILE_CONTENT, "SHA-1");
		Metadata metadata = new Metadata();
		metadata.setPath(localFileName);
		metadata.setRelativeUrl("examples/demo.bin");
		metadata.setSize(FILE_CONTENT.length);
		metadata.setSha1(sha1);

		Repo repo = new Repo();
		repo.setRepositories(repositories);
		repo.setResources(Collections.singletonList(metadata));

		DownloaderContainer container = new DownloaderContainer();
		container.setRepo(repo);
		container.setDestinationRepositories(destination);
		container.setHandlers(Collections.singletonList(new PostHandlerImpl()));

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(Timeout.ofMilliseconds(10_000))
				.setResponseTimeout(Timeout.ofMilliseconds(10_000))
				.build();
		Downloader fileDownloader = new DownloaderImpl(new EventBus(), client, requestConfig,
				new InternetServerMap(), false);
		fileDownloader.addContainer(container);
		fileDownloader.startDownload(true);

		File saved = new File(destination, localFileName);
		assertTrue(saved.exists());
		assertEquals(FILE_CONTENT.length, saved.length());
		assertEquals(sha1, DesktopUtil.getChecksum(saved, "SHA-1"));
	}
}
