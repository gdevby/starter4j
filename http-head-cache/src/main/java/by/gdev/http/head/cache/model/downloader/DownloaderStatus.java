/**
 * 
 */
package by.gdev.http.head.cache.model.downloader;

import java.time.Duration;
import java.util.List;

import com.google.common.eventbus.EventBus;

import lombok.Data;

/**
 * Contains information about current status of the downloading. Will send with
 * {@link EventBus} every second in {@link DownloaderStatusEnum#WORK} status
 * 
 * @author Robert Makrytski
 *
 */

@Data
public class DownloaderStatus {
	/**
	 * Times was passed after start. 
	 */
	private Duration duration;
	/**
	 * Speed in KB
	 */
	private Long speed;
	private Integer leftFiles;
	private Integer allFiles;
	private List<Throwable> throwables;
	private DownloaderStatusEnum downloaderStatusEnum;

}
