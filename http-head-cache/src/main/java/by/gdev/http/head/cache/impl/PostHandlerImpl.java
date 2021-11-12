package by.gdev.http.head.cache.impl;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import by.gdev.http.head.cache.handler.PostHandler;
import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {
	// TODO where do we can extract variable? 
	private String pathToDownload;

	@Override
	public void portProcessDownloadElement(DownloadElement element)  {
		try {
			String shaLocalFile = DesktopUtil.getChecksum(new File(pathToDownload + element.getMetadata().getPath()),Headers.SHA1.getValue());
			long sizeLocalFile = new File(pathToDownload + element.getMetadata().getPath()).length();
			if(sizeLocalFile != element.getMetadata().getSize()) {
				System.out.println("The size of the file is not equal: " + element.getMetadata().getPath());
			}
			if (!shaLocalFile.equals(element.getMetadata().getSha1())) {
				//TODO use logger
				System.out.println("The hash sum of the file is not equal: " + element.getMetadata().getPath());
				// TODO set the problem to element.setThrowable...
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
		}
	}
}