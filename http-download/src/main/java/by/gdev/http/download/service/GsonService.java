package by.gdev.http.download.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Converted json file content of the file to java object
 */

public interface GsonService {
	/**
	 * @return Get local object if exist or return null;
	 */
	<T> T getLocalObject(List<String> uris, String urn, Class<T> class1) throws IOException, NoSuchAlgorithmException;

	/**
	 * @param <T>    type return object
	 * @param urls   Information about the location of the resource being loaded.
	 *               Left side of the URI
	 * @param urn    A value used to identify a resource by its name. Right side of
	 *               the URI
	 * @param class1 java object
	 * @param cache  If cache true file exists and hashsum is valid it should return
	 *               content without head request, dependence on
	 *               {@see FileCacheServiceImpl#timeToLife }. If cache false we need
	 *               to do http head request to check version in the cache with ETag
	 * @return T
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * 
	 */
	<T> T getObjectByUrls(List<String> urls, String urn, Class<T> class1, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException;

	/**
	 * @see #getObjectByUrls(List, String, Class, boolean)
	 */
	<T> T getObjectByUrls(List<String> urls, String urn, Type type, boolean cache)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException;

	/**
	 * Do request to server and return
	 * 
	 * @throws IOException
	 */
	<T> T getObjectWithoutSaving(List<String> urls, String urn, Class<T> classs1) throws IOException;

	/**
	 * @see #getObjectWithoutSaving(List, String, Type)
	 */
	<T> T getObjectWithoutSaving(List<String> urls, String urn, Class<T> classs1, Map<String, String> headers)
			throws IOException;

	/**
	 * @see GsonService#getObjectWithoutSaving(String, Class)
	 */
	<T> T getObjectWithoutSaving(List<String> urls, String urn, Type type) throws IOException;

	/**
	 * @see #getObjectWithoutSaving(List, String, Type)
	 */
	<T> T getObjectWithoutSaving(List<String> urls, String urn, Type type, Map<String, String> headers)
			throws IOException;
}