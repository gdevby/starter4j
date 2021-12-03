package by.gdev.generator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigModel{
	@Parameter(names = "-name", description = "Application Name")
    private String appName;	
	@Parameter(names = "-version", description = "Application version")
    private double appVersion;	
	@Parameter(names = "-mainClass", description = "The main class for running the application")
    private String mainClass;  
	//TODO PARAM TRY TO IMPROVE NAME
	@Parameter(names = "-app", description = "Application arguments")
    private List<String> appArguments;
	//TODO TOO
	@Parameter(names = "-jvm", description = "Arguments for jvm")
	 private List<String> jvmArguments;
	//TODO appJar 
    @Parameter(names = "-appFile", description = "Jar app file to run the application")
    private String appFile;    
    //TODO ADDED EXPLANATION WHY IT SHOULD SKIP
    @Parameter(names = "-javafolder", description = "Input directory where jvm are stored to create configuration for java, lets you skip java generation if the argument is -flag=true")
    private String javaFolder;   
    @Parameter(names = "-javaConfig", description = "Directory where saved the result of the jvm configuration. We can create once the config and using all times.")
    private String javaConfig;    
    @Parameter(names = "-resources", description = "Directory with the necessary resources to run the application")
    private String appResources;   
    @Parameter(names = "-dependencies", description = "Directory with the necessary dependencies to run the application")
    private String appDependencies;   
    //TODO EXPLAIN
    @Parameter(names = "-appfolder", description = "Input directory to generated configuration for new application")
    private String appFolder;   
    //TODO IMPROVE 
    @Parameter(names = "-domain", description = "Domain for which configs will be available for download")
    private List<String> domain;  
    @Parameter(names = "-flag", description = "Flag to skip java generation")
    private boolean generetedJava;   
    @Parameter(names = "-help", help = true)
    public boolean help = false;
    
	public static final AppConfigModel DEFAULT_APP_CONFIG_MODEL;	
	static {		
    	DEFAULT_APP_CONFIG_MODEL = new AppConfigModel(
    			"starter-app",
    			1.0, 
    			"by.gdev.app.Main", 
    			Arrays.asList("",""),
    			Arrays.asList("-Xmx512m","-Dfile.encoding=UTF8"),
    			"starter-app-1.0.jar", 
    			"../../starter-app/jvms",
    			"src/test/resources", 
    			"../../starter-app/src/main/resources/resources", 
    			"../../starter-app/dep", 
    			"../../starter-app", 
    			Arrays.asList("http://localhost:81/"),
    			false, 
    			false);
    }
}