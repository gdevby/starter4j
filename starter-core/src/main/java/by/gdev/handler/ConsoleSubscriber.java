package by.gdev.handler;

import com.google.common.eventbus.Subscribe;

public class ConsoleSubscriber {
    @Subscribe
    public void massage(String s) {
        System.out.println(s);
    }
}
