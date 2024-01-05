package by.gdev.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;

import by.gdev.util.model.download.Repo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JavaProcessHelper {
	private final String jvmPath;
	private final List<String> commands;
    private File directory;
    private ProcessBuilder process;
    private EventBus listener;
    ProcessMonitor monitor;
    
	public JavaProcessHelper(String jvmPath, File directory, EventBus listener) {
		this.jvmPath = jvmPath;
    	this.directory = directory;
    	this.listener = listener;
    	this.commands = new ArrayList<>();
    }
    
    public void start() throws IOException {
        monitor = new ProcessMonitor(createProcess().start(), listener);
        monitor.start();
    }

    public void destroyProcess () {
    	this.monitor.getProcess().destroyForcibly();
    }
    
    
    public ProcessBuilder createProcess() {
        if (Objects.isNull(process)) 
            process = new ProcessBuilder(getFullCommands()).directory(this.directory).redirectErrorStream(true);
        String javaOption = findJavaOptionAndGetName();
        if (Objects.nonNull(javaOption)) {
            process.environment().put(javaOption, "");
        }
        String runCommand = process.command().stream().collect(Collectors.joining(" "));
        log.info("start command {}",runCommand);
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

    public List<Path> librariesForRunning(String workDirectory, Repo fileRepo, Repo dependencis) {
    	List<Path> list = new ArrayList<Path>();
    	dependencis.getResources().forEach(dep->{
			list.add(Paths.get(workDirectory, dep.getPath()));
    	});
    	fileRepo.getResources().forEach(core->{
			list.add(Paths.get(workDirectory, core.getPath()));
    	});
    	return list;
    }
}