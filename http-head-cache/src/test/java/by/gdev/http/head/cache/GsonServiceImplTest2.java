package by.gdev.http.head.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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


public class GsonServiceImplTest2 {
	@Test
	public void test1() throws NoSuchAlgorithmException, IOException {
		Gson gson = new Gson();
		Path pathFile = new FileServiceImpl(new HttpServiceImpl(), gson, StandardCharsets.UTF_8).getRawObject("https://gdev.by/repo/test.json", true);
		BufferedReader read = new BufferedReader(new FileReader(pathFile.toFile()));
		MyTestType type1 =  gson.fromJson(read, MyTestType.class);
		MyTestType type2 = new MyTestType();
		type2.setS("string");
		assertEquals(type1, type2);
		assertNotNull(type1);
	}
}