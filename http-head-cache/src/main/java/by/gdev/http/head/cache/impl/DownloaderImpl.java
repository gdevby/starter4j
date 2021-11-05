/**
 * 
 */
package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import by.gdev.http.cache.exeption.StatusExeption;
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
	/**
	 * Put new elements to download them.
	 */
	private Queue<DownloadElement> downloadElements = new ConcurrentLinkedQueue<>();
	/**
	 * Put processed elements after process to get info about status
	 */
	private List<DownloadElement> processedElements = Collections.synchronizedList(new ArrayList<DownloadElement>());

	private volatile DownloaderStatusEnum status;

	public DownloaderImpl(String pathToDownload) {
		this.pathToDownload = pathToDownload;
		status = DownloaderStatusEnum.IDLE;
	}

	@Override
	public void addContainer(DownloaderContainer container) {
		container.getRepo().getResources().forEach(metadata -> {
			DownloadElement element = new DownloadElement();
			element.setMetadata(metadata);
			element.setRepo(container.getRepo());
			downloadElements.add(element);
		});
	}

	@Override
	public void startDownload(boolean sync) throws InterruptedException, ExecutionException, StatusExeption {
		status = DownloaderStatusEnum.IDLE;
		if (status.equals(DownloaderStatusEnum.IDLE)) {
			List<CompletableFuture<Void>> listThread = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				listThread.add(CompletableFuture.runAsync(() -> {
					status = DownloaderStatusEnum.WORK;
					while (status.equals(DownloaderStatusEnum.WORK)) {
						if (status.equals(DownloaderStatusEnum.CANCEL)) {
							System.out.println("Загрузка прервана");
							break;
						} else {
							DownloadElement element = downloadElements.poll();
							processedElements.add(element);
							if (Objects.nonNull(element)) {
								try {
									download(element);
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InterruptedException | ExecutionException e1) {
									e1.printStackTrace();
								}
							} else {
								break;
							}
						}
					}
					status = DownloaderStatusEnum.IDLE;
				}));
				status = DownloaderStatusEnum.IDLE;
			}
			if (sync) {
				CompletableFuture.allOf(listThread.toArray(new CompletableFuture[0])).get();
			}else {
				CompletableFuture.runAsync(()->{
					try {
						CompletableFuture.allOf(listThread.toArray(new CompletableFuture[0])).get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}).get();
			}
		}else throw new StatusExeption(status.toString());
		
		// это для многопоточки
		// fill downloadElements
		// check status of the downloading
		// start new thread
		// wait 100 ms and check status again and generate every second new stats
		// before exit send DownloaderStatus with status idle
		
	}

	@Override
	public void cancelDownload() {
		status = DownloaderStatusEnum.CANCEL;
	}

	private void download(DownloadElement element) throws IOException, InterruptedException, ExecutionException {
		BufferedInputStream in = null;
		BufferedOutputStream bout = null;
		try {
			URL web = new URL(element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl());
			File file = new File(pathToDownload + element.getMetadata().getPath());
			if (!file.exists())
				file.getParentFile().mkdirs();
			HttpURLConnection http = (HttpURLConnection) web.openConnection();
			Long fileSize = http.getContentLengthLong();
			in = new BufferedInputStream(http.getInputStream());
			FileOutputStream fos = new FileOutputStream(file);
			bout = new BufferedOutputStream(fos, 1024);
			byte[] buffer = new byte[1024];
			int read = 0;
			Long download = 0L;
			while ((read = in.read(buffer, 0, 1024)) >= 0) {
				if (status.equals(DownloaderStatusEnum.CANCEL)) {
					System.out.println("Загрузка прервана");
					break;
				}else {
					bout.write(buffer, 0, read);
					download += read;
//					System.out.println("download file:" + Paths.get(element.getMetadata().getPath()).getFileName() + ". Uploaded "+ download + " bytes for " + fileSize + " bytes");
				}
			}
		} finally {
			bout.close();
			in.close();
		}
	}
}