package by.gdev.handler;

import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsoleSubscriber {
    @Subscribe
    public void massage(String s) {
    	log.info(s);
    }
}
