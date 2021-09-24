package by.gdev.http.head.cache.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
	
	Path getRawObject(String url, boolean cache) throws IOException;
}
