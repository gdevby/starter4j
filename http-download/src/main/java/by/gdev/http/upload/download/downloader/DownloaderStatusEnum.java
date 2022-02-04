/**
 * 
 */
package by.gdev.http.upload.download.downloader;

import by.gdev.http.download.service.Downloader;

/**
 * Transfer of the state
 * 1)idle->work->done->idle
 * 2)idle->work->cancel->idle
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
	 * Sent this events when {@link Downloader} finished job.
	 */
	DONE,
	/**
	 * Tried to cancel downloading
	 */
	CANCEL
}
