package by.gdev.http.download.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import by.gdev.http.upload.download.downloader.DownloadElement;
import lombok.extern.slf4j.Slf4j;
/**
 * Checks if the downloaded file is a symbolic link. If the file is a symbolic link, it creates it
 * @author Robert Makrytski
 *
 */
@Slf4j
public class SimvolicLinkHandler implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement e) {
		if (!e.getMetadata().getLink().equals("")) {
			try {
			Path target = Paths.get(e.getPathToDownload(), e.getMetadata().getLink());
			Path link = Paths.get(e.getPathToDownload(), e.getMetadata().getPath());
			if (Files.exists(link)) {
				Files.delete(link);
			}
			Files.createSymbolicLink(link.toAbsolutePath(), target.toAbsolutePath());
		} catch (IOException ex) {
			log.error("Error to create simvolic link", ex);
		}
		}

	}
}