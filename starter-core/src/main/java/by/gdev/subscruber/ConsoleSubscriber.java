package by.gdev.subscruber;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.head.cache.model.downloader.DownloaderStatus;
import by.gdev.model.StatusModel;
import by.gdev.model.ValidationExceptionMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSubscriber {
    @Subscribe
    public void message(String s) {
    	log.info(s);
    }

    @Subscribe
    public void messageToSpeed(DownloaderStatus status) {
    	log.info("download speed {} MB/s, upload file: {}, from {},  Uploaded {} MB from {} MB",
    			String.format("%.1f", status.getSpeed()),status.getLeftFiles(), status.getAllFiles(), status.getDownloadSize()/1048576, status.getAllDownloadSize()/1048576);
    }
    
    @Subscribe
    private void procces(StatusModel status) {
    	if (status.getLine().equals("starter can be closed"))
    		System.exit(0);
    	else
			log.info(String.valueOf(status.getProcess()));
	}
    
    @Subscribe
    public void valodateMessage(ValidationExceptionMessage message) {
    	log.error(message.printValidationMessage());
    }
}