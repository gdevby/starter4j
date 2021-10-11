/**
 * 
 */
package by.gdev.http.head.cache.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.service.Downloader;

/**
 * @author Robert Makrytski
 *
 */
public class DownloaderImpl implements Downloader {
	/**
	 * Put new elements to process them.
	 */
	private Queue<DownloadElement> downloadElements = new ConcurrentLinkedQueue<>();
	/**
	 * Put processed elements after process to get info about status
	 */
	private List<DownloadElement> processedElements = Collections.synchronizedList(new ArrayList<DownloadElement>());

	@Override
	public void addContainer(DownloaderContainer container) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDownload() {

		// это для многопоточки
		// fill downloadElements
		// check status of the downloading
		// start new thread
		// wait 100 ms and check status again and generate every second new stats
		// before exit send DownloaderStatus with status idle
	}

	@Override
	public void cancelDownload() {
		// TODO Auto-generated method stub

	}

}
