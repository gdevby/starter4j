package by.gdev.http.head.cache;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import by.gdev.http.head.cache.impl.FileServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;

public class MainTest {
    static Gson GSON = new Gson();
    static Charset CHARSET = StandardCharsets.UTF_8;
    
    public static void main(String[] args) throws IOException {
        HttpService httpService = new HttpServiceImpl();
        FileService fileService = new FileServiceImpl(httpService, GSON, CHARSET);
        GsonService gsonService = new GsonServiceImpl(GSON, fileService);	
        String url = "https://gdev.by/repo/test.json";
        MyTestType myTest = gsonService.getObject(url, MyTestType.class);
        System.out.println(myTest);
    }
}
