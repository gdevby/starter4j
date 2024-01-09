package by.gdev.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import com.google.common.eventbus.EventBus;

import by.gdev.model.ExceptionMessage;
import by.gdev.model.StarterAppProcess;
import by.gdev.util.DesktopUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class allows you to monitor the state of the application.
 * 
 * @author Robert Makrytski
 *
 */
@Slf4j
public class ProcessMonitor extends Thread {
	@Getter
	private Process process;
	private EventBus listener;

	public ProcessMonitor(Process process, EventBus listener) {
		this.process = process;
		this.listener = listener;
	}

	@Override
	public void run() {
		InputStreamReader reader = new InputStreamReader(process.getInputStream());
		BufferedReader buf = new BufferedReader(reader);
		String line;
		StarterAppProcess status = new StarterAppProcess();
		while (process.isAlive()) {
			try {
				while (Objects.nonNull(line = buf.readLine())) {
					status.setLine(line);
					status.setProcess(process);
					listener.post(status);
				}
			} catch (IOException t) {
				DesktopUtil.sleep(1);
				status.setProcess(this.process);
				status.setExeption(t);
				listener.post(status);
				try {
					int exitValue = process.exitValue();
					log.trace("Exit value = {}", exitValue);
				} catch (IllegalThreadStateException s) {
					listener.post(new ExceptionMessage(s.getMessage()));
					log.warn("warn", s);
				}
			} finally {
				try {
					IOUtils.close(buf);
				} catch (IOException e) {
					log.error("Error", e);
				}
			}
		}
		status.setErrorCode(process.exitValue());
		listener.post(status);
	}
}