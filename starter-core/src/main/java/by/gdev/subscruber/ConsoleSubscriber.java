package by.gdev.subscruber;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.head.cache.model.downloader.DownloaderStatus;
import by.gdev.model.StatusModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSubscriber {
    @Subscribe
    public void message(String s) {
    	log.info(s);
    }

    @Subscribe
    public void messageToSpeed(DownloaderStatus status) {
    	log.info("download speed {} KB/m, upload file: {}, from {},  Uploaded {} KB from {} KB",
    			String.format("%.2f", status.getSpeed()),status.getLeftFiles(), status.getAllFiles(), status.getDownloadSize(), status.getAllDownloadSize());
    }
    
    @Subscribe
    private void test(StatusModel status) {
    	if (status.getLine().equals("starter can be closed"))
    		System.exit(0);
    	else
			log.info(String.valueOf(status.getProcess()));
	}
}