/**
 * 
 */
package by.gdev.http.download.handler;

import by.gdev.http.upload.download.downloader.DownloadElement;

/**
 * Handler to check the uploaded file for errors
 * @author Robert Makrytski
 *
 */
public interface PostHandler {
	
	void  postProcessDownloadElement(DownloadElement e);
}
