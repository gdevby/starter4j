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

/**
 * Checks the hash sum and size of the downloaded file with the value specified
 * in the config file
 * 
 * @author Robert Makrytski
 *
 */
@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement element) throws NoSuchAlgorithmException, IOException {

		Path localeFile = Paths.get(element.getPathToDownload(), element.getMetadata().getPath());
		String shaLocalFile = DesktopUtil.getChecksum(localeFile.toFile(), Headers.SHA1.getValue());
		long sizeLocalFile = localeFile.toFile().length();
		if (sizeLocalFile != element.getMetadata().getSize() && StringUtils.isEmpty(element.getMetadata().getLink())) {
			element.setError(new HashSumAndSizeError(element.getMetadata().getRelativeUrl(),
					element.getPathToDownload() + element.getMetadata().getPath(), String.format(
							"The size should be %s, but was %s", element.getMetadata().getSize(), sizeLocalFile)));
			Files.deleteIfExists(localeFile.toAbsolutePath());
		}

		if (!shaLocalFile.equals(element.getMetadata().getSha1())
				&& StringUtils.isEmpty(element.getMetadata().getLink())) {
			element.setError(new HashSumAndSizeError(
					element.getRepo().getRepositories().get(0) + element.getMetadata().getRelativeUrl(),
					element.getPathToDownload() + element.getMetadata().getPath(), String.format(
							"The hash sum should be %s, but was %s", element.getMetadata().getSha1(), shaLocalFile)));
			Files.deleteIfExists(localeFile.toAbsolutePath());
		}
	}
}