package by.gdev.model;

import lombok.Data;
import java.util.List;

import by.gdev.util.model.download.Repo;

/**
 * Updating for every version.
 */
@Data
// TODO remove todo if we don't need
//todo put config file in git and use from git (RAW button) ,parse with gson like one object
//todo create real example to run test.jar and add new module with test and description
//todo added this way to config arguments , in this case we can insert in app. -Djava.library.path=${natives_directory}
public class AppConfig {
    private String comment = "Config file for desktop-starter example";
    private String appName;
    private double appVersion;
    private String mainClass;   
//    private Map<ArgumentType,List<String>> arguments;   
    private List<String> appArguments;
    private List<String> jvmArguments;   
    /**
     * images and other files are used an app
     */
    //!
    private Repo appFileRepo;    
    private Repo appResources;
    //used for -cp
    private Repo appDependencies;
    //!
    private Repo javaRepo;
}

