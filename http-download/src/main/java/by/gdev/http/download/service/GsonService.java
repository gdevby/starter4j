package by.gdev.http.download.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import by.gdev.util.model.download.Metadata;

/**
 * Converted json file content of the file to java object
 */

public interface GsonService {
	/**
	 * @param <T>    type return object
	 * @param uri    - uri address
	 * @param class1 java object
	 * @param cache  If cache true file exists and hashsum is valid it should return
	 *               content without head request. If cache false we need to do http
	 *               head request to check version in the cache with ETag.
	 * @return T
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * 
	 * If we dont't have internet it should return old value.
	 */
	<T> T getObject(String uri, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException;
	
	
	<T> T getObjectByUrls(List<String> url, Class<T> class1, boolean cache)
			throws IOException, NoSuchAlgorithmException;
	
	/**
	 * @param <T>
	 * @param uris
	 * @param class1
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	<T> T getLocalObject(List<String> uris, Class<T> class1) throws IOException, NoSuchAlgorithmException ;
	
	/**
	 * @param <T>    type return object
	 * @param urls   Information about the location of the resource being loaded.
	 *               Left side of the URI
	 * @param urn    A value used to identify a resource by its name. Right side of
	 *               the URI
	 * @param class1 java object
	 * @param cache  If cache true file exists and hashsum is valid it should return
	 *               content without head request. If cache false we need to do http
	 *               head request to check version in the cache with ETag
	 * @return T
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * 
	 * 	 * If we dont't have internet it should return old value.
	 */
	<T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException;
	
	
	<T> T getObjectByUrls(List<String> urls, List<Metadata> urns, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException;

	/**
	 * @param <T>     type return object
	 * @param url     Information about the location of the resource being loaded.
	 *                Left side of the URI
	 * @param classs1 java object
	 * @return T
	 * @throws IOException
	 */
	<T> T getObjectWithoutSaving(String url, Class<T> classs1) throws IOException;

	<T> T getObjectWithoutSaving(String url, Class<T> classs1, Map<String, String> headers) throws IOException;

	/**
	 * @see GsonService#getObjectWithoutSaving(String, Class)
	 */
	<T> T getObjectWithoutSaving(String url, Type type) throws IOException;

	<T> T getObjectWithoutSaving(String url, Type type, Map<String, String> headers) throws IOException;
}