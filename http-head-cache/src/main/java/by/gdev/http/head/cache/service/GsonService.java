package by.gdev.http.head.cache.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
//every interface should have description.
public interface GsonService {
//todo you need to define every param of the method.
	/**
	 * 
	 * @param <T>
	 * @param url
	 * @param class1
	 * @param cache
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	<T> T getObject(String url, Class<T> class1, boolean cache) throws FileNotFoundException, IOException, NoSuchAlgorithmException;
}
