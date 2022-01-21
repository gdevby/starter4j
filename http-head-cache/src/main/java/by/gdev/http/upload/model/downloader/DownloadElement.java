/**
 * 
 */
package by.gdev.http.upload.model.downloader;

import java.time.LocalTime;
import java.util.List;

import by.gdev.http.upload.handler.PostHandler;
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
	private String pathToDownload;
	/**
	 * Download part of the file
	 */
	private LocalTime start;
	private LocalTime end;
	private Repo repo;
	private volatile long downloadBytes;
	private volatile Throwable error;

}
