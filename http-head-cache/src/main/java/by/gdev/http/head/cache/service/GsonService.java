package by.gdev.http.head.cache.service;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface GsonService {

	<T> T getObject(String url, Class<T> class1) throws FileNotFoundException, IOException;
}
