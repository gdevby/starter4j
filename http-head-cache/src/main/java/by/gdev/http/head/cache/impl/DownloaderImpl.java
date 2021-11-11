/**
 * 
 */
package by.gdev.http.head.cache.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import com.google.common.eventbus.EventBus;

import by.gdev.http.cache.exeption.StatusExeption;
import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
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
	/**
	 * Put new elements to download them.
	 */
	private Queue<DownloadElement> downloadElements = new ConcurrentLinkedQueue<>();
	/**
	 * Put processed elements after process to get info about status
	 */
	private List<DownloadElement> processedElements = Collections.synchronizedList(new ArrayList<DownloadElement>());

	private volatile DownloaderStatusEnum status;

	public DownloaderImpl(EventBus eventBus) {
		this.eventBus = eventBus;
		status = DownloaderStatusEnum.IDLE;
	}

	@Override
	public void addContainer(DownloaderContainer container) {
		pathToDownload = container.getDestinationRepositories();
//		PostHandlerImpl postHandler = new PostHandlerImpl(pathToDownload);
		container.getRepo().getResources().forEach(metadata -> {
			DownloadElement element = new DownloadElement();
			element.setHandlers(container.getHandlers());
			element.setMetadata(metadata);
			element.setRepo(container.getRepo());
			downloadElements.add(element);
			
		});
	}

	@Override
	public void startDownload(boolean sync) throws InterruptedException, ExecutionException, StatusExeption, IOException {
		DownloadedRunnableImpl runnable = new DownloadedRunnableImpl(status, pathToDownload, downloadElements, eventBus, processedElements);
		status = DownloaderStatusEnum.IDLE;
		if (status.equals(DownloaderStatusEnum.IDLE)) {
			List<CompletableFuture<Void>> listThread = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				listThread.add(CompletableFuture.runAsync(runnable));
			}
			if (sync) {
				synchronous(listThread);
			} else {
				asynchronous(listThread);
			}
		} else
			throw new StatusExeption(status.toString());
	}

	@Override
	public void cancelDownload() {
		status = DownloaderStatusEnum.CANCEL;
	}

	private double averagSpeed() {
		double sum = 0;
		for (DownloadElement d : processedElements) {
			sum += d.getDownloadBytes();
		}
		return sum / processedElements.size();
	}
	
	private void synchronous(List<CompletableFuture<Void>> listThread) throws InterruptedException {
		LocalTime start = LocalTime.now();
		boolean workedAnyThread = true;
		while (workedAnyThread) {
			workedAnyThread = false;
			Thread.sleep(50);
			boolean result = listThread.stream().allMatch(e -> !e.isDone());
			if (result)
				workedAnyThread = true;
			else
				status = DownloaderStatusEnum.IDLE;
			LocalTime now = LocalTime.now();
			if (now.getSecond() == start.getSecond() + 1) {
				eventBus.post(averagSpeed());
				start = now;
			}
		}
	}
	
	private void asynchronous(List<CompletableFuture<Void>> listThread) throws InterruptedException, ExecutionException {
		CompletableFuture.runAsync(() -> {
			try {
				LocalTime start = LocalTime.now();
				boolean workedAnyThread = true;
				while (workedAnyThread) {
					workedAnyThread = false;
					Thread.sleep(50);
					boolean result = listThread.stream().allMatch(e -> !e.isDone());
					if (result)
						workedAnyThread = true;
					LocalTime now = LocalTime.now();
					if (now.getSecond() == start.getSecond() + 1) {
						eventBus.post(averagSpeed());
						start = now;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).get();
	}
}