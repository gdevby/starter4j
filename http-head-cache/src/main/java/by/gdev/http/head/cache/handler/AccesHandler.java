package by.gdev.http.head.cache.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.OSType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccesHandler implements PostHandler{

	@Override
	public void postProcessDownloadElement(DownloadElement e) {
		if (e.getMetadata().isExecutable())
			if (OSInfo.getOSType() == OSType.LINUX | OSInfo.getOSType() == OSType.MACOSX) {
			     try {
					Files.setPosixFilePermissions(Paths.get(e.getPathToDownload() + e.getMetadata().getPath()), DesktopUtil.PERMISSIONS);
				} catch (IOException e1) {
					log.error("Error", e1);
				}
			}
	}

}