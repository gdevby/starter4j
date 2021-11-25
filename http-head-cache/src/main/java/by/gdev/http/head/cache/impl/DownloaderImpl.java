/**
 * 
 */
package by.gdev.http.head.cache.impl;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

/**
 * @author Robert Makrytski
 *
 */

@Data
@AllArgsConstructor
public class DownloaderImpl implements Downloader {
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

	private volatile DownloaderStatusEnum status;

	public DownloaderImpl(EventBus eventBus,CloseableHttpClient httpclient ,RequestConfig requestConfig) {
		this.eventBus = eventBus;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		status = DownloaderStatusEnum.IDLE;
	}

	@Override
	public void addContainer(DownloaderContainer container) {
		container.getRepo().getResources().forEach(metadata -> {
			DownloadElement element = new DownloadElement();
			element.setPathToDownload(container.getDestinationRepositories());
			element.setHandlers(container.getHandlers());
			element.setMetadata(metadata);
			element.setRepo(container.getRepo());
			downloadElements.add(element);

		});
	}

	@Override
	public void startDownload(boolean sync) throws InterruptedException, ExecutionException, StatusExeption {
		if (status.equals(DownloaderStatusEnum.IDLE) || status.equals(DownloaderStatusEnum.CANCEL)) {
			status = DownloaderStatusEnum.WORK;
			DownloadedRunnableImpl runnable = new DownloadedRunnableImpl(status, downloadElements, processedElements, httpclient, requestConfig);
			List<CompletableFuture<Void>> listThread = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				listThread.add(CompletableFuture.runAsync(runnable));
			}
			if (sync) {
				synchronous(listThread);
			} else {
				CompletableFuture.runAsync(() -> {
					try {
						synchronous(listThread);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).get();
			}
		} else
			throw new StatusExeption(status.toString());
	}

	@Override
	public void cancelDownload() {
		status = DownloaderStatusEnum.CANCEL;
	}

	private DownloaderStatus averagSpeed() {
		DownloaderStatus statusDownload = new DownloaderStatus();
		double sum = 0;
		for (DownloadElement d : processedElements) {
			sum += d.getDownloadBytes();
		}
		statusDownload.setSpeed(sum / processedElements.size());
		return statusDownload;
	}
	
	private void synchronous(List<CompletableFuture<Void>> listThread) throws InterruptedException {
		LocalTime start = LocalTime.now();
		boolean workedAnyThread = true;
		while (workedAnyThread) {
			workedAnyThread = false;
			Thread.sleep(50);
			workedAnyThread = listThread.stream().allMatch(e -> !e.isDone());
			if (!workedAnyThread)
				status = DownloaderStatusEnum.DONE;
			LocalTime now = LocalTime.now();
			if (now.getSecond() == start.getSecond() + 1) {
				eventBus.post(averagSpeed().getSpeed());
				start = now;
			}
		}
	}
}