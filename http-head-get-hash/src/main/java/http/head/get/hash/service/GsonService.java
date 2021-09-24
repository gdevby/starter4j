package http.head.get.hash.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GsonService {
	Gson GSON;
	FileService fileService;
	
	public GsonService(Gson GSON, FileService fileService) {
		this.GSON = GSON;
		this.fileService = fileService;
	}
	
	public <T> T getObject(String url, Class<T> class1) throws JsonSyntaxException, IOException {
		return fileService.read(fileService.getRawObject(url, false), class1);
	}
}
