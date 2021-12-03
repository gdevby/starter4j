package by.gdev.subscruber;

import com.google.common.eventbus.Subscribe;

import by.gdev.process.JavaProcess;
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
    
//    @Subscribe
//    public void onJavaProcessLog(JavaProcess process, String line) {
//    	System.out.println(process.getClass().getName() +"" + line);
//    	//TODO ????? added log!
//    	log.info(process , line);
//    }
//    
//    
//    @Subscribe
//    public void onJavaProcessEnded(JavaProcess process) {
//    	System.out.println(process.getClass().getName());
//    	//TODO ????? added log too!
//    	log.info(process , line);
//    }
}