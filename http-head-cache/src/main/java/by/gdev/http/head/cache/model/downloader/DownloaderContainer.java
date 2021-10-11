package by.gdev.http.head.cache.model.downloader;

import java.util.List;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.util.model.download.Repo;
import lombok.Data;

/**
 * Saved status of the download elements and additional.
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class DownloaderContainer {
	private Repo repo;
	private List<PostHandler> handlers;

}
