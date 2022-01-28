package by.gdev.subscruber;

import java.util.Objects;
import java.util.ResourceBundle;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.upload.model.downloader.DownloaderStatus;
import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppProcess;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ConsoleSubscriber {
	private ResourceBundle bundle;
	//TODO remove
    @Subscribe
    public void message(String s) {
    	log.info(s);
    }

    @Subscribe
    public void downloadStatusMessage(DownloaderStatus status) {
    	if (status.getLeftFiles() != 0)
    	log.info(String.format(bundle.getString("upload.speed"), String.format("%.1f", status.getSpeed()),status.getLeftFiles(), status.getAllFiles(), status.getDownloadSize()/1048576, status.getAllDownloadSize()/1048576));
    }

    @Subscribe
    private void procces(StarterAppProcess status) {
    	if (Objects.nonNull(status.getErrorCode())) {
    		if (status.getErrorCode() == -1073740791)
    			log.error(bundle.getString("driver.error"));
    		else if (status.getErrorCode() == -1073740771)
    			log.error(bundle.getString("msi.afterburner.error"));
    		else if (status.getErrorCode() != 0)
    			log.error(bundle.getString("unidentified.error"));
    	}
    	else if (status.getLine().equals("starter can be closed"))
    		System.exit(0);
    	else
			log.info(String.valueOf(status.getLine()));
	}
    
    @Subscribe
    public void validateMessage(ExceptionMessage message) {
    	log.error(message.printValidationMessage());
    }
}