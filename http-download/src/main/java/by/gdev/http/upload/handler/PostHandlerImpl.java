package by.gdev.http.upload.handler;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import by.gdev.http.upload.exeption.HashSumAndSizeError;
import by.gdev.http.upload.model.Headers;
import by.gdev.http.upload.model.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * Checks the hash sum and size of the downloaded file with the value specified in the config file
 * @author Robert Makrytski
 *
 */
@Slf4j
@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement element) {
		try {
			String shaLocalFile = DesktopUtil.getChecksum(
					new File(element.getPathToDownload() + element.getMetadata().getPath()), Headers.SHA1.getValue());
			long sizeLocalFile = new File(element.getPathToDownload() + element.getMetadata().getPath()).length();
			if (sizeLocalFile != element.getMetadata().getSize()
					&& StringUtils.isEmpty(element.getMetadata().getLink()))
				element.setError(new HashSumAndSizeError(element.getMetadata().getRelativeUrl(),
						element.getPathToDownload() + element.getMetadata().getPath(),
						"The size should be " + element.getMetadata().getSize()));
			if (!shaLocalFile.equals(element.getMetadata().getSha1())
					&& StringUtils.isEmpty(element.getMetadata().getLink()))
				element.setError(new HashSumAndSizeError(
						element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl(),
						element.getPathToDownload() + element.getMetadata().getPath(),
						"The hash sum should be " + element.getMetadata().getSha1()));
		} catch (IOException | NoSuchAlgorithmException e) {
			log.error("Erorr", e);
		}
	}
}