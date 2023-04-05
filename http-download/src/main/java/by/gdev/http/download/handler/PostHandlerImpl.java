package by.gdev.http.download.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import by.gdev.http.download.exeption.HashSumAndSizeError;
import by.gdev.http.download.model.Headers;
import by.gdev.http.upload.download.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks the hash sum and size of the downloaded file with the value specified
 * in the config file
 * 
 * @author Robert Makrytski
 *
 */
@Slf4j
@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement element) {
		try {

			Path localeFile = Paths.get(element.getPathToDownload(), element.getMetadata().getPath());
			String shaLocalFile = DesktopUtil.getChecksum(localeFile.toFile(), Headers.SHA1.getValue());
			long sizeLocalFile = localeFile.toFile().length();
			if (sizeLocalFile != element.getMetadata().getSize()
					&& StringUtils.isEmpty(element.getMetadata().getLink())) {
				element.setError(new HashSumAndSizeError(element.getMetadata().getRelativeUrl(),
						element.getPathToDownload() + element.getMetadata().getPath(),
						"The size should be " + element.getMetadata().getSize()));
				removeFile(localeFile);
			}

			if (!shaLocalFile.equals(element.getMetadata().getSha1())
					&& StringUtils.isEmpty(element.getMetadata().getLink())) {
				element.setError(new HashSumAndSizeError(
						element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl(),
						element.getPathToDownload() + element.getMetadata().getPath(),
						"The hash sum should be " + element.getMetadata().getSha1()));
				removeFile(localeFile);
			}

		} catch (IOException | NoSuchAlgorithmException e) {
			log.error("Erorr", e);
		}
	}

	private void removeFile(Path localeFile) {
		try {
			Files.delete(localeFile.toAbsolutePath());
		} catch (IOException e) {
			log.error("file can't be deleted {}", e.getMessage());
		}
	}
}