package by.gdev.http.download.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import by.gdev.http.upload.download.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.OSType;

/**
 * A handler that checks whether the uploaded file is executable. If yes, then
 * sets the execution permissions
 * 
 * @author Robert Makrytski
 *
 */
public class AccessHandler implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement e) throws IOException {
		if (e.getMetadata().isExecutable())
			if (OSInfo.getOSType() == OSType.LINUX | OSInfo.getOSType() == OSType.MACOSX) {
				Files.setPosixFilePermissions(Paths.get(e.getPathToDownload() + e.getMetadata().getPath()),
						DesktopUtil.PERMISSIONS);
			}
	}
}