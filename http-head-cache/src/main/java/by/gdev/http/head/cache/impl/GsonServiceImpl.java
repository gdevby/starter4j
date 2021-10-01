package by.gdev.http.head.cache.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;

import by.gdev.http.head.cache.service.FileService;
import by.gdev.http.head.cache.service.GsonService;

//todo description from interface
public class GsonServiceImpl implements GsonService {
	// todo private
	Gson gson;
	FileService fileService;

	public GsonServiceImpl(Gson GSON, FileService fileService) {
		this.gson = GSON;
		this.fileService = fileService;
	}

	@Override
	public <T> T getObject(String url, Class<T> class1, boolean cache) throws IOException, NoSuchAlgorithmException {
		Path pathFile = fileService.getRawObject(url, cache);
		try (BufferedReader read = new BufferedReader(new FileReader(pathFile.toFile()))) {
			return gson.fromJson(read, class1);
		}
	}
}
