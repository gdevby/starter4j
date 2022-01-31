/**
 * 
 */
package by.gdev.http.upload.handler;

import by.gdev.http.upload.model.downloader.DownloadElement;

/**
 * Handler to check the uploaded file for errors
 * @author Robert Makrytski
 *
 */
public interface PostHandler {
	
	void  postProcessDownloadElement(DownloadElement e);
}
