package by.gdev.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;

import lombok.Data;

@Data
public class JavaProcessHelper {
	private final String jvmPath;
	private final List<String> commands;
    private File directory;
    private ProcessBuilder process;
    private EventBus listener;

    
	public JavaProcessHelper(String jvmPath, File directory, EventBus listener) {
		this.jvmPath = jvmPath;
    	this.directory = directory;
    	this.listener = listener;
    	this.commands = new ArrayList<>();
    }
    
    public JavaProcess start() throws IOException {
        List<String> full = getFullCommands();
        return new JavaProcess(full, createProcess().start(), listener);
    }
    
    private ProcessBuilder createProcess() {
        if (Objects.isNull(process)) 
            process = new ProcessBuilder(getFullCommands()).directory(this.directory).redirectErrorStream(true);
        String javaOption = findJavaOptionAndGetName();
        if (Objects.nonNull(javaOption)) {
            process.environment().put(javaOption, "");
        }
        return process;
    }
    
    private List<String> getFullCommands() {
        List<String> result = new ArrayList<>(this.commands);
        result.add(0, jvmPath);
        return result;
    }    
    
    private static String findJavaOptionAndGetName() {
        for (Map.Entry<String, String> e : System.getenv().entrySet())
            if (e.getKey().equalsIgnoreCase("_java_options"))
                return e.getKey();
        return null;
    }
     
    public void addCommand(String command) {
        this.commands.add(command);
    }

    public void addCommand(String key, String value) {
        this.commands.add(key);
        this.commands.add(value);
    }
    
    public void addCommands(List<String> list) {
        for (String c : list)
            this.commands.add(c);
    }
    
    public List<Path> librariesForRunning(Path p) throws IOException{
    	return Files.walk(p, 2).filter(e -> !e.equals(p) && String.valueOf(e).endsWith(".jar")).map(e -> e.toAbsolutePath()).collect(Collectors.toList());
    }
}
