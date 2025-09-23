package by.gdev.http.upload.download.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import by.gdev.http.download.handler.PostHandler;
import by.gdev.http.download.model.Headers;
import by.gdev.http.download.service.Downloader;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Saved status of the download elements and additional to send To
 * {@link Downloader#addContainer(DownloaderContainer)}.
 * 
 * @author Robert Makrytski
 *
 */

@Data
@Slf4j
public class DownloaderContainer {
	String destinationRepositories;
	long containerSize;
	Repo repo;
	List<PostHandler> handlers;

	public void filterNotExistResoursesAndSetRepo(Repo repo, String workDirectory)
			throws IOException {
		this.repo = new Repo();
		List<Metadata> listRes = new ArrayList<Metadata>();
		for (Metadata meta : repo.getResources()) {
			File localFile = Paths.get(workDirectory, meta.getPath()).toAbsolutePath().toFile();
			if (localFile.exists()) {
				String shaLocalFile = DesktopUtil.getChecksum(localFile, Headers.SHA1.getValue());
				BasicFileAttributes attr = Files.readAttributes(localFile.toPath(), BasicFileAttributes.class,
						LinkOption.NOFOLLOW_LINKS);
				if (!attr.isSymbolicLink() & !shaLocalFile.equals(meta.getSha1())) {
					listRes.add(meta);
					log.warn("The hash sum of the file is not equal. File " + localFile + " will be deleted. Size = "
							+ localFile.length() / 1024 / 1024);
					Files.delete(localFile.toPath());
				}
			} else {
				listRes.add(meta);
			}
		}
		this.repo.setResources(listRes);
		this.repo.setRepositories(repo.getRepositories());
	}

	public void containerAllSize(Repo repo) {
		containerSize = repo.getResources().stream().map(Metadata::getSize).reduce(Long::sum).orElse(0L);
	}
}