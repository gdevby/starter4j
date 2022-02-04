package by.gdev.http.upload.model.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import by.gdev.http.upload.handler.PostHandler;
import by.gdev.http.upload.model.Headers;
import by.gdev.http.upload.service.Downloader;
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
	private String destinationRepositories;
	private long containerSize;
	private Repo repo;
	private List<PostHandler> handlers;

	public void filterNotExistResoursesAndSetRepo(Repo repo, String workDirectory)
			throws NoSuchAlgorithmException, IOException {
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

	public void conteinerAllSize(Repo repo) {
		List<Long> sizeList = new ArrayList<Long>();
		repo.getResources().forEach(size -> {
			sizeList.add(size.getSize());
		});
		long sum = 0;
		for (long l : sizeList) {
			sum += l;
		}
		this.containerSize = sum;
	}
}