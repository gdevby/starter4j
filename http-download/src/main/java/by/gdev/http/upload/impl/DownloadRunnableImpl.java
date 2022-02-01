package by.gdev.http.upload.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.eventbus.EventBus;

import by.gdev.http.upload.exeption.UploadFileException;
import by.gdev.http.upload.model.downloader.DownloadElement;
import by.gdev.http.upload.model.downloader.DownloaderStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
/**
 * This class implements the launch of file downloads by URI
 */

public class DownloadRunnableImpl implements Runnable {
	private volatile DownloaderStatusEnum status;
	private Queue<DownloadElement> downloadElements;
	private List<DownloadElement> processedElements;
	private CloseableHttpClient httpclient;
	private RequestConfig requestConfig;
	private EventBus eventBus;
	private int DEFAULT_MAX_ATTEMPTS = 3;

	public DownloadRunnableImpl(Queue<DownloadElement> downloadElements, List<DownloadElement> processedElements, CloseableHttpClient httpclient, RequestConfig requestConfig, EventBus eventBus) {
		this.downloadElements = downloadElements;
		this.processedElements = processedElements;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		this.eventBus = eventBus;
	}
	
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
						element.getHandlers().forEach(post -> post.postProcessDownloadElement(element));
					}catch(Throwable e1) {
						element.setError(new UploadFileException(element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl(),
								element.getMetadata().getPath(), e1.getLocalizedMessage()));
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * A method that allows a byte-by-byte download of a file at the specified URI
	 * @param element item received from the download queue
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	
	private void download(DownloadElement element) throws IOException, InterruptedException {
		processedElements.add(element);
		File file = new File(element.getPathToDownload() + element.getMetadata().getPath());
			for (int attempt = 0; attempt < DEFAULT_MAX_ATTEMPTS; attempt++) {
				try {
					BufferedInputStream in = null;
					BufferedOutputStream out = null;
					boolean resume = false;
					HttpGet httpGet = new HttpGet(element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl());
					log.trace(String.valueOf(httpGet));
					try {
						if (!file.getParentFile().exists())
							file.getParentFile().mkdirs();
						if (file.exists() && Objects.nonNull(element.getMetadata().getSha1())
								&& file.length() != element.getMetadata().getSize()) {
							httpGet.addHeader("Range",
									"bytes= " + file.length() + "-" + element.getMetadata().getSize());
							resume = true;
						}
						httpGet.setConfig(requestConfig);
						CloseableHttpResponse response = httpclient.execute(httpGet);
						HttpEntity entity = response.getEntity();
						in = new BufferedInputStream(entity.getContent());
						out = new BufferedOutputStream(new FileOutputStream(file, resume));
						
						byte[] buffer = new byte[1024];
						int curread = in.read(buffer);
						while (curread != -1) {
							if (status.equals(DownloaderStatusEnum.CANCEL)) {
								break;
							} else {
								out.write(buffer, 0, curread);
								curread = in.read(buffer);
								element.setDownloadBytes(element.getDownloadBytes() + curread);
								
							}
						}
						eventBus.post(element);
						LocalTime endTime = LocalTime.now();
						element.setEnd(endTime);
						DEFAULT_MAX_ATTEMPTS = 1;
					} finally {
						httpGet.abort();
						IOUtils.close(out);
						IOUtils.close(in);
					}
				} catch (SocketTimeoutException e) {
					if (attempt == DEFAULT_MAX_ATTEMPTS)
						throw new SocketTimeoutException();
//					else
//						attempts++;
				}
			}	
	}
}