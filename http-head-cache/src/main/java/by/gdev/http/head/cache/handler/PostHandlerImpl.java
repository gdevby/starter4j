package by.gdev.http.head.cache.handler;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import by.gdev.http.head.cache.exeption.ThrowableExeption;
import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement element)  {
		try {
			String shaLocalFile = DesktopUtil.getChecksum(new File(element.getPathToDownload() + element.getMetadata().getPath()),Headers.SHA1.getValue());
			long sizeLocalFile = new File(element.getPathToDownload() + element.getMetadata().getPath()).length();
			
			if(sizeLocalFile != element.getMetadata().getSize()) 
				element.setError(new ThrowableExeption("The size of the file is not equal: " + element.getMetadata().getPath()));
			
			if (!shaLocalFile.equals(element.getMetadata().getSha1())) 
				element.setError(new ThrowableExeption("The hash sum of the file is not equal: " + element.getMetadata().getPath()));
		} catch (IOException | NoSuchAlgorithmException e) {
			log.error("Erorr", e);
		}
	}
}