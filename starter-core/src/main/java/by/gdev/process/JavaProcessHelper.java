package by.gdev.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.EventBus;

import lombok.Data;

@Data
public class JavaProcessHelper {
	private String jvmPath;
    private List<String> commands;
    private File directory;
    private ProcessBuilder process;
    private EventBus listener;

    private List<String> getFullCommands() {
        List<String> result = new ArrayList<>(this.commands);
        result.add(0, jvmPath);
        return result;
    }
    
    public ProcessBuilder createProcess() {
//        if (process == null)
//            process = new ProcessBuilder(getFullCommands()).directory(
//                    this.directory).redirectErrorStream(true);
//        String javaOption = TlauncherUtil.findJavaOptionAndGetName();
//        if (Objects.nonNull(javaOption)) {
//            process.environment().put(javaOption, "");
//        }
        return process;
    }
    
    
    
    
    public JavaProcess start() throws IOException {
        List<String> full = getFullCommands();
        return new JavaProcess(full, createProcess().start(), listener);
    }
    
    
    
}
