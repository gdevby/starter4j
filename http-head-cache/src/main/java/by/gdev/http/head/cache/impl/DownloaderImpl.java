/**
 * 
 */
package by.gdev.http.head.cache.impl;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.eventbus.EventBus;

import by.gdev.http.head.cache.exeption.StatusExeption;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.model.downloader.DownloaderStatus;
import by.gdev.http.head.cache.model.downloader.DownloaderStatusEnum;
import by.gdev.http.head.cache.service.Downloader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Robert Makrytski
 *	This class is responsible for the state of the file upload
 */

@Slf4j
@Data
@AllArgsConstructor
public class DownloaderImpl implements Downloader {
	/**
	 * Path to download file
	 */
	private String pathToDownload;
	private EventBus eventBus;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	/**
	 * Put new elements to download them.
	 */
	private Queue<DownloadElement> downloadElements = new ConcurrentLinkedQueue<>();
	/**
	 * Put processed elements after process to get info about status
	 */
	private List<DownloadElement> processedElements = Collections.synchronizedList(new ArrayList<DownloadElement>());
	/**
	 * Shown status of the downloading
	 */
	private volatile DownloaderStatusEnum status;
	private DownloadRunnableImpl runnable;
	private volatile Integer allCountElement;
	private volatile long fullDownloadSize;
	
	public DownloaderImpl(EventBus eventBus,CloseableHttpClient httpclient ,RequestConfig requestConfig) {
		this.eventBus = eventBus;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		status = DownloaderStatusEnum.IDLE;
		runnable = new DownloadRunnableImpl(downloadElements, processedElements, httpclient, requestConfig);
	}

	@Override
	public void addContainer(DownloaderContainer container) {
		if (Objects.nonNull(container.getRepo().getResources())) {
			container.getRepo().getResources().forEach(metadata -> {
				DownloadElement element = new DownloadElement();
				element.setMetadata(metadata);
				element.setRepo(container.getRepo());
				element.setPathToDownload(container.getDestinationRepositories());
				element.setHandlers(container.getHandlers());
				downloadElements.add(element);
			});
		}
		fullDownloadSize = totalSize(downloadElements);
	}

	@Override
	public void startDownload(boolean sync) throws InterruptedException, ExecutionException, StatusExeption {
		if (status.equals(DownloaderStatusEnum.IDLE) || status.equals(DownloaderStatusEnum.CANCEL)) {
			status = DownloaderStatusEnum.WORK;
			runnable.setStatus(status);
			allCountElement = downloadElements.size();
			List<CompletableFuture<Void>> listThread = new ArrayList<>();
			for (int i = 0; i < 3; i++) 
				listThread.add(CompletableFuture.runAsync(runnable));
			if (sync) {
				waitThreadDone(listThread);
			} else {
				CompletableFuture.runAsync(() -> {
					try {
						waitThreadDone(listThread);
					} catch (InterruptedException e) {
						log.error("Error", e);
					}
				}).get();
			}	
		} else
			throw new StatusExeption(status.toString());
	}

	@Override
	public void cancelDownload() {
		status = DownloaderStatusEnum.CANCEL;
		runnable.setStatus(DownloaderStatusEnum.CANCEL);
	}

	private DownloaderStatus averageSpeed() {
		DownloaderStatus statusDownload = new DownloaderStatus();
		double sum = 0;
		long downloadBytesNow = 0;
		List<DownloadElement> list = new ArrayList<DownloadElement>(processedElements);
		for (DownloadElement elem : list) {
			double thirty = Duration.between(elem.getStart(), elem.getEnd()).getNano();
			double speed = (elem.getDownloadBytes() / 1024) / (thirty / 60000000);
			if (speed == Double.NaN || speed == Double.NEGATIVE_INFINITY || speed == Double.POSITIVE_INFINITY)
	    		 speed = 5.0;
		    sum += speed;
		    downloadBytesNow += elem.getDownloadBytes();
		    statusDownload.setDownloadSize(downloadBytesNow);
	     }
	    statusDownload.setAllDownloadSize(fullDownloadSize);
	    statusDownload.setLeftFiles(processedElements.size());
		statusDownload.setAllFiles(allCountElement);
		statusDownload.setSpeed(sum / processedElements.size());
		return statusDownload;
	}
	
	private void waitThreadDone(List<CompletableFuture<Void>> listThread) throws InterruptedException {
		LocalTime start = LocalTime.now();
		boolean workedAnyThread = true;
		while (workedAnyThread) {
			workedAnyThread = false;
			Thread.sleep(50);
			workedAnyThread = listThread.stream().anyMatch(e -> !e.isDone());
			if (start.isBefore(LocalTime.now())) {
				start = start.plusSeconds(3);
				if (allCountElement != 0) {
					log.info(start.toString());
					eventBus.post(averageSpeed());
				}
			}
		}
	}
	
	private long totalSize(Queue<DownloadElement> downloadElements) {
		List<Long> sizeList = new ArrayList<Long>();
		downloadElements.forEach(size->{
			sizeList.add(size.getMetadata().getSize());
		});
		long sum = 0;
		for (long l : sizeList) {
			sum +=l;
		}
		return sum;
	}
}