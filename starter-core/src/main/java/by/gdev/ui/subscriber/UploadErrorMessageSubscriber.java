package by.gdev.ui.subscriber;

import java.util.ResourceBundle;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.exeption.HashSumError;
import by.gdev.http.upload.exeption.UploadFileException;
import by.gdev.http.upload.model.downloader.DownloaderStatus;
import by.gdev.http.upload.model.downloader.DownloaderStatusEnum;
import by.gdev.model.ValidationExceptionMessage;
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
					eventBus.post(new ValidationExceptionMessage(s));
				}else if (t instanceof HashSumError) {
					HashSumError t1 = (HashSumError) t;
					String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri());
					eventBus.post(new ValidationExceptionMessage(s));
				}
			}
		}
	}

}
