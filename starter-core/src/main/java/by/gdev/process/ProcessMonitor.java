package by.gdev.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import com.google.common.eventbus.EventBus;

import by.gdev.model.StatusModel;
import by.gdev.util.DesktopUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * This class allows you to monitor the state of the application.
 * 
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
		//TODO зачем два цикла?
		while (this.process.isRunning()) {
			try {
				while (Objects.nonNull(line = buf.readLine())) {
					StatusModel status = new StatusModel();
					status.setLine(line);
					status.setProcess(process);
					listener.post(status);
				}
			} catch (IOException t) {
				DesktopUtil.sleep(1);
				StatusModel status = new StatusModel();
				status.setErrorCode(raw.exitValue());
				status.setExeption(t);
            	listener.post(status);
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