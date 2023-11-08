package by.gdev.http.upload.download.downloader;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DownloadFile {

	private String uri;
	private String file;
}
