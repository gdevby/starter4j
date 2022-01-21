/**
 * 
 */
package by.gdev.http.upload.handler;

import by.gdev.http.upload.model.downloader.DownloadElement;

/**
 * @author Robert Makrytski
 *
 */
public interface PostHandler {
	
	void  postProcessDownloadElement(DownloadElement e);
}
