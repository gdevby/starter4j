package by.gdev.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;

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
	            try {
	                while ((line = buf.readLine()) != null) {
	                    if (listener != null) {
	                    	listener.post(line);
	                    	listener.post(process);
	                    }
	                }
	            } catch (Throwable ex) {
	            } finally {
	                try {
	                    buf.close();
	                } catch (IOException ex) {
	                    Logger.getLogger(ProcessMonitor.class.getName()).log(Level.SEVERE, null, ex);
	                }
	            }
	        }
	        if (Objects.nonNull(listener))
	            listener.post(this.process);
	    }
}
