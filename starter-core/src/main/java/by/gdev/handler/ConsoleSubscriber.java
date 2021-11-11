package by.gdev.handler;

import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSubscriber {
    @Subscribe
    public void message(String s) {
    	log.info(s);
    }
    
    @Subscribe
    public void messageToSpeed(Double d) {
    	System.out.println("download speed: " + String.format("%.2f", d) + "KB/m");
    }
}
