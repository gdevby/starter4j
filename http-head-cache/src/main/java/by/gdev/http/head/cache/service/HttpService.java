package by.gdev.http.head.cache.service;

import java.io.IOException;
import java.nio.file.Path;

import by.gdev.http.head.cache.model.RequestMetadata;

public interface HttpService {
	RequestMetadata getResourseByUrlAndSave(String url, Path path) throws IOException;
}
