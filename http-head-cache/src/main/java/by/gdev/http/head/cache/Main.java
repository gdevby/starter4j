package by.gdev.http.head.cache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.MyTestType;


public class Main {
	static Gson GSON = new Gson();
	static Charset CHARSET = StandardCharsets.UTF_8;
	
	public static void main(String[] args) throws JsonSyntaxException, IOException {
		HttpServiceImpl httpService = new HttpServiceImpl();
		FileServiceImpl fileService = new FileServiceImpl(httpService, GSON, CHARSET);
		GsonServiceImpl gsonService = new GsonServiceImpl(GSON, fileService);
		
		String url = "http://localhost:81/t.json";
		MyTestType myTest = gsonService.getObject(url, MyTestType.class);
		System.out.println(myTest);
	}
}