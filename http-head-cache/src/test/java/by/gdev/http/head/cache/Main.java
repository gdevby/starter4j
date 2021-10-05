package by.gdev.http.head.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
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
import com.google.gson.JsonSyntaxException;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.MyTestType;
import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	
	static GsonService gsonService;
	static HttpService httpService;
	
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
                        log.info("used keep alive 5000");
                        return 5000L;
                    }
                    return -1;
                }).setConnectionManager(cm).evictIdleConnections(10, TimeUnit.SECONDS).build();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(604800).setSocketTimeout(604800).build();
		HttpService httpService = new HttpServiceImpl(builder, requestConfig);
		FileService fileService = new FileServiceImpl(httpService, gson, StandardCharsets.UTF_8, testFolder, 6048000);
		gsonService = new GsonServiceImpl(gson, fileService);
	}
	
	@Test
	public void main() throws JsonSyntaxException, IOException, NoSuchAlgorithmException {
		MyTestType myTest = gsonService.getObject("https://gdev.by/repo/test.json", MyTestType.class, false);
		System.out.println(myTest);
	}
}