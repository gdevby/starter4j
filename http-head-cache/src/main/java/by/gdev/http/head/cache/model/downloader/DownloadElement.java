/**
 * 
 */
package by.gdev.http.head.cache.model.downloader;

import java.util.List;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import lombok.Data;

/**
 * Described all info to download  for one file
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class DownloadElement {
	private List<PostHandler> handlers;
	private Metadata metadata;
	/**
	 * Download part of the file
	 */
	private Repo repo;
	private volatile Long downloadBytes;
	private volatile Throwable t;

}
