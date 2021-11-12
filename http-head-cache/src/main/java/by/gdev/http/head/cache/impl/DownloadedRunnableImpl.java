package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

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
		// TODO: test 
		PostHandlerImpl postHandler = new PostHandlerImpl(pathToDownload);
		// TODO:  test ?
		status = DownloaderStatusEnum.WORK;
		while (status.equals(DownloaderStatusEnum.WORK)) {
			if (status.equals(DownloaderStatusEnum.CANCEL)) {
				// TODO: ?
				eventBus.post("Download interrupted");
				break;
			} else {
				DownloadElement element = downloadElements.poll();
				if (Objects.nonNull(element)) {
					try {
						download(element);
						// TODO: when we add aditional handlers do they work or call?
						element.getHandlers().get(0).portProcessDownloadElement(element);
					} catch (IOException e) {
						e.printStackTrace();
						// TODO: ?
					} catch (NoSuchAlgorithmException e1) {
						e1.printStackTrace();
					}
				} else {
					break;
				}
			}
		}
	}

	
	//Догрузка: получать даннные с сервера
	/**
	 * TODO: It should try 3 times if the net is okay. Test defferent sites for access and regulated param from 3 to one. 
	 * @param element
	 * @throws IOException
	 */
	private void download(DownloadElement element) throws IOException {
		BufferedInputStream in = null;
		BufferedOutputStream bout = null;
		try {
			LocalTime startTime = LocalTime.now();
			element.setStart(startTime);
			// TODO: add test with two repository when first repo is 404 return
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
			// TODO: WHY sleep?
			Thread.sleep(100);
//			Thread.sleep(500);
			while ((read = in.read(buffer, 0, 1024)) != -1) {
				if (status.equals(DownloaderStatusEnum.CANCEL)) {
					eventBus.post("Download interrupted");
					break;
				} else {
					// TODO: how does it work? what is lenght of the read in this case?
					bout.write(buffer, 0, read);
					element.setDownloadBytes(element.getDownloadBytes() + read);
					//use log not system out 
					System.out.println("download file:" + Paths.get(element.getMetadata().getPath()).getFileName() 
							+ ". Uploaded "+ Double.valueOf(element.getDownloadBytes()).longValue() 
							+ " bytes for " + fileSize + " bytes");
				}
			}
			LocalTime endTime = LocalTime.now();
			element.setEnd(endTime);
			long thirty = Duration.between(startTime, endTime).getNano();
			double speed = (element.getDownloadBytes() / 1024) / (thirty / 60000000);
			// TODO: where you should calculate speed?
			if (speed < 1) {
				speed = 0.1;
			}
			// TODO: why? how does it work with one gb file?
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