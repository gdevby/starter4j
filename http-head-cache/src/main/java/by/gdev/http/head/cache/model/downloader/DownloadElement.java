/**
 * 
 */
package by.gdev.http.head.cache.model.downloader;

import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import lombok.Data;

/**
 * Described all info to download 
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class DownloadElement {
	private DownloaderContainer container;
	private Metadata metadata;
	/**
	 * Download part of the file
	 */
	private Repo repo;
	private volatile Long downloadBytes;
	private volatile Throwable t;

}
