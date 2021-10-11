/**
 * 
 */
package by.gdev.http.head.cache.service;

import com.google.common.eventbus.EventBus;

import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.model.downloader.DownloaderStatus;

/**
 * Allow to download files and show status of the downloading. Statistics and
 * Status o fthe {@link Downloader} you can get with {@link EventBus} and class
 * {@link DownloaderStatus}.
 * 
 * @author Robert Makrytski
 *
 */
public interface Downloader {
	/**
	 * Add new type of the download files. You can add additional operation after
	 * download of the file
	 * 
	 * @param container
	 */
	void addContainer(DownloaderContainer container);

	void startDownload();

	void cancelDownload();

}
