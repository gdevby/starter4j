package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderStatusEnum;
import ch.qos.logback.core.recovery.ResilientSyslogOutputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class DownloadedRunnableImpl implements Runnable {
	private volatile DownloaderStatusEnum status;
	private Queue<DownloadElement> downloadElements;
	private List<DownloadElement> processedElements;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private final int DEFAULT_MAX_ATTEMPTS = 3;

	@Override
	public void run() {
		while (status.equals(DownloaderStatusEnum.WORK)) {
			if (status.equals(DownloaderStatusEnum.CANCEL)) {
				break;
			} else {
				DownloadElement element = downloadElements.poll();
				if (Objects.nonNull(element)) {
					try {
						download(element);
						element.getHandlers().forEach(post -> {
							post.postProcessDownloadElement(element);
						});
						if (Objects.nonNull(element.getT()))
							log.error(element.getT().toString());
					} catch (IOException e) {
						log.error("Exeption", e);
					} catch (InterruptedException e1) {
						log.error("Exeption", e1);
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * TODO: It should try 3 times if the net is okay. Test defferent sites for
	 * access and regulated param from 3 to one.
	 * 
	 * @param element
	 * @throws IOException
	 * @throws InterruptedException
	 */

	private void download(DownloadElement element) throws IOException, InterruptedException {
		File file = new File(element.getPathToDownload() + element.getMetadata().getPath());
		if (file.length() != element.getMetadata().getSize()){
			int attempt = 0;
			while (attempt < DEFAULT_MAX_ATTEMPTS) {
				try {
					BufferedInputStream in = null;
					BufferedOutputStream out = null;
					boolean resume = false;
					try {
						LocalTime startTime = LocalTime.now();
						element.setStart(startTime);
						HttpGet httpGet = new HttpGet(element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl());
						if (!file.exists()) {
							file.getParentFile().mkdirs();
						}
						if (file.exists())
							if (Objects.nonNull(element.getMetadata().getSha1()))
								if (file.length() != element.getMetadata().getSize()) {
									httpGet.addHeader("Range", "bytes= " + file.length() + "-" + element.getMetadata().getSize());
									resume = true;
								}
						httpGet.setConfig(requestConfig);
						CloseableHttpResponse response = httpclient.execute(httpGet);
						HttpEntity entity = response.getEntity();
						in = new BufferedInputStream(entity.getContent());
						out = new BufferedOutputStream(new FileOutputStream(file, resume));
						byte[] buffer = new byte[1024];
//						Thread.sleep(100); 
						int curread = in.read(buffer);
						while (curread != -1) {
							if (status.equals(DownloaderStatusEnum.CANCEL)) {
								break;
							} else {
								out.write(buffer, 0, curread);
								curread = in.read(buffer);
								element.setDownloadBytes(element.getDownloadBytes() + curread);
								log.info("download file:" + Paths.get(element.getMetadata().getPath()).getFileName() + ". Uploaded "
										+ Double.valueOf(element.getDownloadBytes()).longValue() + " bytes for "
										+ response.getFirstHeader(Headers.CONTENTLENGTH.getValue()).getValue() + " bytes");
							}
						}
						LocalTime endTime = LocalTime.now();
						element.setEnd(endTime);
						long thirty = Duration.between(startTime, endTime).getNano();
						double speed = (element.getDownloadBytes() / 1024) / (thirty / 60000000);
						element.setDownloadBytes(speed);
						processedElements.add(element);
					} finally {
						out.close();
						in.close();
					}
					attempt = 3;
				}catch (SocketTimeoutException e) {
					attempt++;
					if (attempt == DEFAULT_MAX_ATTEMPTS)
						throw new SocketTimeoutException();
					else 
						continue;	
				}
			}
			
			
		}
		
		
		
		
		
	}
}