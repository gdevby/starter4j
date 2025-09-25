package by.gdev.http.download.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.eventbus.EventBus;

import by.gdev.http.download.exeption.UploadFileException;
import by.gdev.http.download.handler.PostHandler;
import by.gdev.http.upload.download.downloader.DownloadElement;
import by.gdev.http.upload.download.downloader.DownloadFile;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.util.model.InternetServerMap;
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

	private InternetServerMap workedServers;
	private int downloadMaxAttemps;
	private boolean resumeDownload;

	public DownloadRunnableImpl(Queue<DownloadElement> downloadElements, List<DownloadElement> processedElements,
			CloseableHttpClient httpclient, RequestConfig requestConfig, EventBus eventBus,
			InternetServerMap workedServers, int downloadMaxAttemps, boolean resumeDownloadFile) {
		this.downloadElements = downloadElements;
		this.processedElements = processedElements;
		this.httpclient = httpclient;
		this.requestConfig = requestConfig;
		this.eventBus = eventBus;
		this.workedServers = workedServers;
		this.downloadMaxAttemps = downloadMaxAttemps;
		this.resumeDownload = resumeDownloadFile;
	}

	@Override
	public void run() {
		while (status.equals(DownloaderStatusEnum.WORK)) {
			if (status.equals(DownloaderStatusEnum.CANCEL)) {
				break;
			} else {
				DownloadElement element = downloadElements.poll();
				if (Objects.nonNull(element)) {
					Throwable ex = null;
					processedElements.add(element);
					for (String repo : workedServers
							.getAliveDomainsOrUseAllWithSort(element.getRepo().getRepositories())) {
						try {
							download(element, repo);
							for (PostHandler h : element.getHandlers()) {
								h.postProcessDownloadElement(element);
							}
							ex = null;
							break;
						} catch (Throwable e1) {
							ex = e1;
						}
					}
					if (Objects.nonNull(ex)) {
						element.setError(ex);
					}
				} else {
//					DesktopUtil.sleep(1000);
					break;
				}
			}
		}
	}

	/**
	 * A method that allows a byte-by-byte download of a file at the specified URI
	 * 
	 * @param element item received from the download queue
	 * @throws IOException
	 * @throws InterruptedException
	 */

	private void download(DownloadElement element, String repo) throws IOException, InterruptedException {
		File file = new File(element.getPathToDownload() + element.getMetadata().getPath());
		String url = null;
		for (int attempt = 0; attempt < downloadMaxAttemps; attempt++) {
			try {
				element.setDownloadBytes(0L);
				BufferedInputStream in = null;
				BufferedOutputStream out = null;
				boolean resume = false;
				url = repo + element.getMetadata().getRelativeUrl();
				log.trace(url);
				HttpGet httpGet = new HttpGet(url);

				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				if (resumeDownload)
					resume = tryDownloadPart(element, file, httpGet);
				try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

					StatusLine sl = response.getStatusLine();

					if (sl.getStatusCode() != HttpStatus.SC_OK) {
						throw new IOException(
								String.format("code %s phrase %s %s", sl.getStatusCode(), sl.getReasonPhrase(), url));
					}
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
					eventBus.post(new DownloadFile(url, file.toString(), element.getDownloadBytes()));
					LocalTime endTime = LocalTime.now();
					element.setEnd(endTime);
					return;
				} finally {
					httpGet.abort();
					IOUtils.closeQuietly(out);
					IOUtils.closeQuietly(in);
				}
			} catch (Exception e) {
				if (attempt == downloadMaxAttemps - 1) {
					throw new UploadFileException(url, file.toString(), e.getMessage());
				}
			}
		}
	}

	private boolean tryDownloadPart(DownloadElement element, File file, HttpGet httpGet)
			throws ClientProtocolException, IOException {
		if (file.exists() && Objects.nonNull(element.getMetadata().getSha1())
				&& file.length() < element.getMetadata().getSize()) {
			HttpHead hh = new HttpHead(httpGet.getURI());
			try (CloseableHttpResponse response = httpclient.execute(hh)) {
				Header h = response.getFirstHeader("accept-ranges");
				if (Objects.nonNull(h) && "bytes".equals(h.getValue())) {
					log.info("download part with accept-ranges current size {}, full size {}", file.length(),
							element.getMetadata().getSize());
					httpGet.addHeader("Range", "bytes= " + file.length() + "-" + element.getMetadata().getSize());
					return true;
				}
			}
		}
		return false;
	}
}