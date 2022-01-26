package by.gdev.model;

import lombok.Data;
import java.util.List;

import by.gdev.util.model.download.Repo;

/**
 * Updating for every version.
 * This class describes the main appconfig
 * @author Robert Makrytski
 */
 
@Data
public class AppConfig {
    private String comment = "Config file for desktop-starter example";
    /**
     * Applications name 
     */
    private String appName;
    /**
     * Applications version, example 0.9.12.10
     */
    private String appVersion;
    /**
     * Main class of the launched application
     */
    private String mainClass;   
    /**
     * Application launch arguments
     */
    private List<String> appArguments;
    /**
     * JVM launch arguments
     */
    private List<String> jvmArguments;   
    /**
     * images and other files are used an app
     */
    /**
     * Description of the jar file to run the application
     */
    private Repo appFileRepo;    
    /**
     * Description of json configuration of resources required to run the application
     */
    private Repo appResources;
   /**
    * Json description of the dependency configuration for running the application
    */
    private Repo appDependencies;
    /**
     * Description JVM json configuration 
     */
    private Repo javaRepo;
}