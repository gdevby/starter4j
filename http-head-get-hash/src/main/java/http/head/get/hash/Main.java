package http.head.get.hash;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import http.head.get.hash.model.MyTestType;
import http.head.get.hash.service.FileService;
import http.head.get.hash.service.GsonService;
import http.head.get.hash.service.HttpService;

public class Main {
	static Gson GSON = new Gson();
	static Charset CHARSET = StandardCharsets.UTF_8;
	
	public static void main(String[] args) throws JsonSyntaxException, IOException {
		HttpService httpService = new HttpService();
		FileService fileService = new FileService(httpService, GSON, CHARSET);
		GsonService gsonService = new GsonService(GSON, fileService);
		
		String url = "http://localhost:81/t.json";
		MyTestType myTest = gsonService.getObject(url, MyTestType.class);
		System.out.println(myTest);
	}
}
