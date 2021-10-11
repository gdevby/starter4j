/**
 * 
 */
package by.gdev.http.head.cache.model.downloader;

import by.gdev.http.head.cache.service.Downloader;

/**
 * Transfer of the state
 * 1)idle->work->done-idle
 * 2)idle->work->cancel-idle
 * Status of the {@link Downloader}.
 * @author Robert Makrytski
 *
 */
public enum DownloaderStatusEnum {
	/**
	 * Can process new tasks.
	 * 
	 */
	IDLE,
	/**
	 * Downloading.
	 */
	WORK,
	/**
	 * Tried to cancel downloading
	 */
	DONE,
	CANCEL
}