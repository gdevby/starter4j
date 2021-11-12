/**
 * 
 */
package by.gdev.http.head.cache.handler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import by.gdev.http.head.cache.model.downloader.DownloadElement;

/**
 * @author Robert Makrytski
 *
 */
public interface PostHandler {
	// TODO translate name of the method???
	void  portProcessDownloadElement(DownloadElement e) throws IOException, NoSuchAlgorithmException;
}
