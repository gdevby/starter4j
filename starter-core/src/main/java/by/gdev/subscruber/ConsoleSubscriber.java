package by.gdev.subscruber;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.download.downloader.DownloadFile;
import by.gdev.http.upload.download.downloader.DownloaderStatus;
import by.gdev.http.upload.download.downloader.DownloaderStatusEnum;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppConfig;
import by.gdev.model.StarterAppProcess;
import by.gdev.utils.service.FileMapperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ConsoleSubscriber {
	private ResourceBundle bundle;
	@SuppressWarnings("unused")
	private FileMapperService fileMapperService;
	private StarterAppConfig starterConfig;

	@Subscribe
	public void downloadStatusMessage(DownloaderStatus status) {
		if (!status.getDownloaderStatusEnum().equals(DownloaderStatusEnum.IDLE))
			if (status.getLeftFiles() != 0)
				log.info(String.format(bundle.getString("upload.speed"), String.format("%.1f", status.getSpeed()),
						status.getLeftFiles(), status.getAllFiles(), status.getDownloadSize() / 1048576,
						status.getAllDownloadSize() / 1048576));
		if (status.getDownloaderStatusEnum().equals(DownloaderStatusEnum.DONE))
			if (status.getThrowables().size() != 0) {
				log.error("error", status.getThrowables().get(0));
				System.exit(-1);
			}
	}

	@Subscribe
	private void procces(StarterAppProcess status) {
		checkUnsatisfiedLinkError(status);
		if (Objects.nonNull(status.getErrorCode())) {
			if (status.getErrorCode() == -1073740791)
				log.error(bundle.getString("driver.error"));
			else if (status.getErrorCode() == -1073740771)
				log.error(bundle.getString("msi.afterburner.error"));
			else if (status.getErrorCode() != 0) {
				log.error(bundle.getString("unidentified.error"));
				System.exit(0);
			}
		} else if (status.getLine().contains("starter can be closed"))
			System.exit(0);
		else
			log.info(String.valueOf(status.getLine()));
	}

	@Subscribe
	public void validateMessage(ExceptionMessage message) {
		log.error(message.printValidationMessage());
		System.exit(-1);
	}

	@Subscribe
	public void downloadedFile(DownloadFile entity) {
		log.info("downloaded file: {} -> {} size {}", entity.getUri(), entity.getFile(), entity.getSize());
	}

	private void checkUnsatisfiedLinkError(StarterAppProcess status) {
		if (!StringUtils.isEmpty(status.getLine())
				&& status.getLine().equals("java.lang.UnsatisfiedLinkError: no zip in java.library.path")) {
			String newWorkDir = "C:\\" + starterConfig.getWorkDirectory();
			log.error(String.format(bundle.getString("unidentified.error"),
					Paths.get(starterConfig.getWorkDirectory()).toAbsolutePath().toString(), newWorkDir));
//			log.error(String.format(bundle.getString("unsatisfied.link.error"),
//					Paths.get(starterConfig.getWorkDirectory()).toAbsolutePath().toString(), newWorkDir));
//			try {
//				AppLocalConfig appLocalConfig = new AppLocalConfig();
//				appLocalConfig = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG,
//						AppLocalConfig.class);
//				log.info(appLocalConfig.toString());
//				fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
//			} catch (IOException e) {
//				log.error("Error ", e);
//			}
		}
	}
}