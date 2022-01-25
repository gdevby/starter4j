package by.gdev.ui.subscriber;

import java.util.ResourceBundle;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.exeption.HashSumAndSizeError;
import by.gdev.http.upload.exeption.UploadFileException;
import by.gdev.http.upload.model.downloader.DownloaderStatus;
import by.gdev.http.upload.model.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UploadErrorMessageSubscriber {
	private ResourceBundle bundle;
	private EventBus eventBus;

	@Subscribe
	public void message(DownloaderStatus status) {
		if (DownloaderStatusEnum.DONE.equals(status.getDownloaderStatusEnum())) {
			if (!status.getThrowables().isEmpty()) {
				Throwable t = status.getThrowables().get(0);
				if (t instanceof UploadFileException) {
					UploadFileException t1 = (UploadFileException) t;
					String s = String.format(bundle.getString("upload.error"), t1.getUri(), t1.getLocalPath(),
							t1.getLocalizedMessage());
					eventBus.post(new ExceptionMessage(s));
				}else if (t instanceof HashSumAndSizeError) {
					HashSumAndSizeError t1 = (HashSumAndSizeError) t;
					String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri());
					eventBus.post(new ExceptionMessage(s));
				}
			}
		}
	}

}
