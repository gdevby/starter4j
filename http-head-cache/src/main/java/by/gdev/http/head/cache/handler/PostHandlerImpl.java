package by.gdev.http.head.cache.handler;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import by.gdev.http.head.cache.model.Headers;
import by.gdev.http.head.cache.model.downloader.DownloadElement;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PostHandlerImpl implements PostHandler {

	@Override
	public void postProcessDownloadElement(DownloadElement element)  {
		try {
			String shaLocalFile = DesktopUtil.getChecksum(new File(element.getPathToDownload() + element.getMetadata().getPath()),Headers.SHA1.getValue());
			long sizeLocalFile = new File(element.getPathToDownload() + element.getMetadata().getPath()).length();
			if(sizeLocalFile != element.getMetadata().getSize()) {
				//todo 
				element.setT(new Throwable("The size of the file is not equal: " + element.getMetadata().getPath()));
			}
			if (!shaLocalFile.equals(element.getMetadata().getSha1())) {
				element.setT(new Throwable("The hash sum of the file is not equal: " + element.getMetadata().getPath()));
			}
		} catch (IOException e1) {
			//TODO 
			e1.printStackTrace();
		}catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
		}
	}
}