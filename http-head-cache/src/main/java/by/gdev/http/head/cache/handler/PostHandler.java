/**
 * 
 */
package by.gdev.http.head.cache.handler;

import by.gdev.http.head.cache.model.downloader.DownloadElement;

/**
 * @author Robert Makrytski
 *
 */
public interface PostHandler {
	
	void  postProcessDownloadElement(DownloadElement e);
}
