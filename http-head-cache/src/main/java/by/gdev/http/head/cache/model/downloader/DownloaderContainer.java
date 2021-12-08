package by.gdev.http.head.cache.model.downloader;

import java.util.List;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.http.head.cache.impl.DownloaderImpl;
import by.gdev.http.head.cache.service.Downloader;
import by.gdev.util.model.download.Repo;
import lombok.Data;

/**
 * Saved status of the download elements and additional to send To {@link Downloader#addContainer(DownloaderContainer)}.
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class DownloaderContainer {
	private String destinationRepositories;
	private Repo repo;
	private List<PostHandler> handlers;
}
