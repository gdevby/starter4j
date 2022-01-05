package by.gdev.http.head.cache.model.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.service.Downloader;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Saved status of the download elements and additional to send To {@link Downloader#addContainer(DownloaderContainer)}.
 * 
 * @author Robert Makrytski
 *
 */
@Data
@Slf4j
public class DownloaderContainer {
	private String destinationRepositories;
	private Repo repo;
	private List<PostHandler> handlers;

	public void filterNotExistResoursesAndSetRepo(Repo repo, String workDirectory) throws NoSuchAlgorithmException, IOException {
		this.repo = new Repo();
		List<Metadata> listRes = new ArrayList<Metadata>();
		for (Metadata meta : repo.getResources()) {
			File localFile = Paths.get(workDirectory, meta.getPath()).toAbsolutePath().toFile();
			if (localFile.exists()) {
				String shaLocalFile = DesktopUtil.getChecksum(localFile,Headers.SHA1.getValue());
				log.trace(meta.getPath() + " = " + shaLocalFile + " / " + meta.getSha1());
				if (!shaLocalFile.equals(meta.getSha1()))
					listRes.add(meta);
			}else
				listRes.add(meta);
		}
		this.repo.setResources(listRes);
		this.repo.setRepositories(repo.getRepositories());
	}
}