/**
 * 
 */
package by.gdev.http.download.impl;

import java.io.IOException;
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

import by.gdev.http.download.exeption.StatusExeption;
import by.gdev.http.download.service.Downloader;
import by.gdev.http.upload.download.downloader.DownloadElement;
import by.gdev.http.upload.download.downloader.DownloaderContainer;
import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.util.InternetServerMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for preparing files for download, displaying
 * information about download status and errors.
 * 
 * @author Robert Makrytski
 */

@Slf4j
@Data
@AllArgsConstructor
public class DownloaderImpl implements Downloader {
	private int downloadMaxAttemps = 1;
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
	private List<Long> allContainerSize = new ArrayList<Long>();
	private volatile DownloaderStatusEnum status;
	private DownloadRunnableImpl runnable;
	private volatile Integer allCountElement;
	private long fullDownloadSize;
	private long downloadBytesNow;
	private LocalTime start;
	private long sizeDownloadNow;

	public DownloaderImpl(EventBus eventBus, CloseableHttpClient httpclient, RequestConfig requestConfig,
			InternetServerMap workedServers) {
		this.eventBus = eventBus;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		status = DownloaderStatusEnum.IDLE;
		runnable = new DownloadRunnableImpl(downloadElements, processedElements, httpclient, requestConfig, eventBus,
				workedServers, downloadMaxAttemps);
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
		pathToDownload = container.getDestinationRepositories();
		allContainerSize.add(container.getContainerSize());
	}

	@Override
	public void startDownload(boolean sync)
			throws InterruptedException, ExecutionException, StatusExeption, IOException {
		downloadElements.stream().map(e -> e.getMetadata().getSize()).reduce(Long::sum).ifPresent(e -> {
			fullDownloadSize = e;
		});

		start = LocalTime.now();
		if (status.equals(DownloaderStatusEnum.IDLE)) {
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

	/**
	 * After stop it should be IDLE
	 */

	@Override
	public void cancelDownload() {
		status = DownloaderStatusEnum.CANCEL;
		runnable.setStatus(DownloaderStatusEnum.CANCEL);
	}

	private DownloaderStatus buildDownloaderStatus() {
		DownloaderStatus statusDownload = new DownloaderStatus();
		long downloadBytesNow = 0;
		List<DownloadElement> list = new ArrayList<DownloadElement>(processedElements);
		List<Throwable> errorList = new ArrayList<Throwable>();
		double thirty = Duration.between(start, LocalTime.now()).getSeconds();
		for (DownloadElement elem : list) {
			downloadBytesNow += elem.getDownloadBytes();
			if (Objects.nonNull(elem.getError()))
				errorList.add(elem.getError());
		}
		statusDownload.setThrowables(errorList);
		statusDownload.setDownloadSize(sizeDownloadNow + downloadBytesNow);
		statusDownload.setSpeed((downloadBytesNow / 1048576) / thirty);
		statusDownload.setDownloaderStatusEnum(status);
		statusDownload.setAllDownloadSize(fullDownloadSize);
		statusDownload.setLeftFiles(processedElements.size());
		statusDownload.setAllFiles(allCountElement);
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
				start = start.plusSeconds(1);
				if (allCountElement != 0) {
					if (start.getSecond() != start.plusSeconds(1).getSecond())
						eventBus.post(buildDownloaderStatus());
				}
			}
		}
		status = DownloaderStatusEnum.DONE;
		eventBus.post(buildDownloaderStatus());
	}
}