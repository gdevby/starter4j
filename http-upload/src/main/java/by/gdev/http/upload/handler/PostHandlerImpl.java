package by.gdev.http.upload.handler;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import by.gdev.http.upload.model.Headers;
import by.gdev.http.upload.model.downloader.DownloadElement;
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
			
//			Objects.isNull(element.getMetadata().getLink())
			
			//TODO 
			if(sizeLocalFile != element.getMetadata().getSize() && Objects.isNull(element.getMetadata().getLink())) {
				element.setError(new Throwable("The size of the file is not equal: " + element.getMetadata().getPath()));
				System.out.println("The size of the file is not equal: " + element.getMetadata().getPath());
			}
			//TODO System.out.println
			if (!shaLocalFile.equals(element.getMetadata().getSha1()) && Objects.isNull(element.getMetadata().getLink())) {
				element.setError(new Throwable("The hash sum of the file is not equal: " + element.getMetadata().getPath()));
				System.out.println("The hash sum of the file is not equal: " + element.getMetadata().getPath());
			}
				
		} catch (IOException | NoSuchAlgorithmException e) {
			log.error("Erorr", e);
		}
	}
}