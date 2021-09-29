package by.gdev.http.head.cache;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.google.gson.Gson;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.service.FileService;


public class GsonServiceImplTest1 {
	Gson gson 
	@Post
	public void init() {
		gson = new Gson();
	}
	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		
		Path pathFile = fileService.getRawObject("https://gdev.by/repo/test.json", false);
		MyTestType type1 =  gson.fromJson(new BufferedReader(new FileReader(pathFile.toFile())), MyTestType.class);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
	}
}