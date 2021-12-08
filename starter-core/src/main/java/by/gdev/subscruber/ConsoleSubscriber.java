package by.gdev.subscruber;

import com.google.common.eventbus.Subscribe;

import by.gdev.http.head.cache.model.downloader.DownloaderStatus;
import by.gdev.process.JavaProcess;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSubscriber {
    @Subscribe
    public void message(String s) {
    	log.info(s);
    }

    @Subscribe
    public void messageToSpeed(DownloaderStatus status) {
    	log.info("download speed: " + String.format("%.2f", status.getSpeed()) + "KB/m");
    }
    
    @Subscribe
    private void onJavaProcessEnded(JavaProcess process) {
    	log.info(String.valueOf(process));
	}
}