package by.gdev.model;

import lombok.Data;
import java.util.List;

import by.gdev.util.model.download.Repo;

/**
 * Updating for every version.
 */
 
@Data
public class AppConfig {
	//TODO DESCRIBE VALUES
    private String comment = "Config file for desktop-starter example";
    private String appName;
    private double appVersion;
    private String mainClass;   
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