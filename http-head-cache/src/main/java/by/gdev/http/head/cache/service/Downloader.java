/**
 * 
 */
package by.gdev.http.head.cache.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.eventbus.EventBus;

import by.gdev.http.cache.exeption.StatusExeption;
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
	 * @throws IOException 
	 */
	void addContainer(DownloaderContainer container) throws IOException;

	void startDownload(boolean sync) throws InterruptedException, ExecutionException, StatusExeption;

	void cancelDownload();
}
