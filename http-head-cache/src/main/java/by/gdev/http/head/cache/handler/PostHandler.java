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
	void  portProcessDownloadElement(DownloadElement e) throws IOException, NoSuchAlgorithmException;
}
