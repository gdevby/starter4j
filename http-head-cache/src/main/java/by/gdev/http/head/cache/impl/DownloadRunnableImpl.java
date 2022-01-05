package by.gdev.http.head.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.http.head.cache.model.downloader.DownloaderStatusEnum;
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
	private final int DEFAULT_MAX_ATTEMPTS = 3;

	public DownloadRunnableImpl(Queue<DownloadElement> downloadElements, List<DownloadElement> processedElements, CloseableHttpClient httpclient, RequestConfig requestConfig) {
		this.downloadElements = downloadElements;
		this.processedElements = processedElements;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
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
						log.error("Error in run method", e1);
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
		File file = new File(element.getPathToDownload() + element.getMetadata().getPath());
		if (file.length() != element.getMetadata().getSize() || element.getMetadata().getSize() == 0){
			for (int attempt = 0; attempt < DEFAULT_MAX_ATTEMPTS; attempt++) {
				try {
					BufferedInputStream in = null;
					BufferedOutputStream out = null;
					boolean resume = false;
					LocalTime startTime = LocalTime.now();
					HttpGet httpGet = new HttpGet(element.getRepo().getRepositories().get(0) 
							+ URLEncoder.encode(element.getMetadata().getRelativeUrl(), StandardCharsets.UTF_8.name()));
					log.trace(String.valueOf(httpGet));
					try {
						element.setStart(startTime);
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
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
						log.trace("downloaded file: "+httpGet.getURI() + " -> " + file);
						LocalTime endTime = LocalTime.now();
						element.setEnd(endTime);
						long thirty = Duration.between(startTime, endTime).getNano();
						double speed = (element.getDownloadBytes() / 1024) / (thirty / 60000000);
						element.setSpeed(speed);
						processedElements.add(element);
					} finally {
						httpGet.abort();
						out.close();
						in.close();
					}
				} catch (SocketTimeoutException e) {
					if (attempt == DEFAULT_MAX_ATTEMPTS)
						throw new SocketTimeoutException();
					else
						attempt++;
				}
			}	
		}
	}
}