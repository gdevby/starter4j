package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.google.common.eventbus.EventBus;

import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderStatusEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DownloadedRunnableImpl implements Runnable {
	private volatile DownloaderStatusEnum status;
	private String pathToDownload;
	private Queue<DownloadElement> downloadElements;
	private EventBus eventBus;
	private List<DownloadElement> processedElements;

	@Override
	public void run() {
		status = DownloaderStatusEnum.WORK;
		while (status.equals(DownloaderStatusEnum.WORK)) {
			if (status.equals(DownloaderStatusEnum.CANCEL)) {
				eventBus.post("Download interrupted");
				break;
			} else {
				DownloadElement element = downloadElements.poll();
				if (Objects.nonNull(element)) {
					try {
						download(element);
						element.getHandlers().get(0).portProcessDownloadElement(element);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					}
				} else {
					break;
				}
			}
		}
	}

	private void download(DownloadElement element) throws IOException {
		BufferedInputStream in = null;
		BufferedOutputStream bout = null;
		try {
			LocalTime startTime = LocalTime.now();
			element.setStart(startTime);
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
			int read;
			Thread.sleep(100);
			while ((read = in.read(buffer, 0, 1024)) != -1) {
				if (status.equals(DownloaderStatusEnum.CANCEL)) {
					eventBus.post("Download interrupted");
					break;
				} else {
					bout.write(buffer, 0, read);
					element.setDownloadBytes(element.getDownloadBytes() + read);
					System.out.println("download file:" + Paths.get(element.getMetadata().getPath()).getFileName() 
							+ ". Uploaded "+ Double.valueOf(element.getDownloadBytes()).longValue() 
							+ " bytes for " + fileSize + " bytes");
				}
			}
			LocalTime endTime = LocalTime.now();
			element.setEnd(endTime);
			long thirty = Duration.between(startTime, endTime).getNano();
			double speed = (element.getDownloadBytes() / 1024) / (thirty / 60000000);
			if (speed < 1) {
				speed = 0.1;
			}
			element.setDownloadBytes(speed);
			processedElements.add(element);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}finally {
			bout.close();
			in.close();
		}
	}
}