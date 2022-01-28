package by.gdev.process;

import java.util.List;

import com.google.common.eventbus.EventBus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
//TODO REVIEW IN FUTURE, try to implement wihtou this
public class JavaProcess {
	private final List<String> commands;
    private final Process process;
    
    public JavaProcess(List<String> commands, Process process, EventBus listener) {
        this.commands = commands;
        this.process = process;
        ProcessMonitor monitor = new ProcessMonitor(this, listener);
        monitor.start();
    }

    public Process getRawProcess() {
        return this.process;
    }


    public boolean isRunning() {
        try {
            this.process.exitValue();
        } catch (IllegalThreadStateException ex) {
            return true;
        }

        return false;
    }

    public int getExitCode() {
        try {
            return this.process.exitValue();
        } catch (IllegalThreadStateException ex) {
            ex.fillInStackTrace();
            throw ex;
        }
    }

    @Override
    public String toString() {
        return "JavaProcess[commands=" + this.commands + ", isRunning="
                + isRunning() + "]";
    }

    public void stop() {
        this.process.destroy();
    }
}
