package by.gdev.http.head.cache.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface GsonService {

	<T> T getObject(String url, Class<T> class1) throws FileNotFoundException, IOException, NoSuchAlgorithmException;
}
