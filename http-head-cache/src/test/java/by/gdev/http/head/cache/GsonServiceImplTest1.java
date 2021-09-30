package by.gdev.http.head.cache;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;


public class GsonServiceImplTest1 {
	GsonService gsonService;
	HttpService httpService;
	
	@Before
	public void init() {
		Gson gson = new Gson();
		httpService = new HttpServiceImpl();
		FileService fileService = new FileServiceImpl(httpService, gson, StandardCharsets.UTF_8);
		gsonService = new GsonServiceImpl(gson, fileService);
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
}