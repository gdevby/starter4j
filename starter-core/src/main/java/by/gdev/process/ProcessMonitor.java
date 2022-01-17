package by.gdev.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import com.google.common.eventbus.EventBus;

import by.gdev.model.StatusModel;
import lombok.extern.slf4j.Slf4j;
/**
 * This class allows you to monitor the state of the application.
 * @author Robert Makrytski
 *
 */
@Slf4j
public class ProcessMonitor extends Thread {
	 private final JavaProcess process;
	 private EventBus listener;

	    public ProcessMonitor(JavaProcess process, EventBus listener) {
	        this.process = process;
	        this.listener = listener;
	    }

	    @Override
	    public void run() {
	        Process raw = this.process.getRawProcess();
	        InputStreamReader reader = new InputStreamReader(raw.getInputStream());
	        BufferedReader buf = new BufferedReader(reader);
	        String line;
	        while (this.process.isRunning()) {
	        	StatusModel status = new StatusModel();
	            try {
	                while (Objects.nonNull(line = buf.readLine())) {
	                	status.setLine(line);
	                	status.setProcess(process);
	                	listener.post(status);
	                }
	            } catch (IOException t) {
	            	status.setExeption(t);
	            	System.exit(0);
	            } finally {
	                try {
	                    buf.close();
	                } catch (IOException e) {
	                    log.error("Error", e);
	                }
	            }
	        }
	    }
}
