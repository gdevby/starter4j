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
	@Parameter(names = "-class", description = "The main class for running the application")
    private String mainClass;   	
	@Parameter(names = "-app", description = "Application arguments")
    private List<String> appArguments; 	
	@Parameter(names = "-jvm", description = "Arguments for jvm")
	 private List<String> jvmArguments;	
    @Parameter(names = "-appFile", description = "Jar app file to run the application")
    private String appFile;    
    @Parameter(names = "-javafolder", description = "Input directory where jvm are stored to create configuration for java")
    private String javaFolder;   
    @Parameter(names = "-config", description = "Saved the result of the jvm configuration")
    private String javaConfig;    
    @Parameter(names = "-resources", description = "Directory with the necessary resources to run the application")
    private String appResources;   
    @Parameter(names = "-dependencies", description = "Directory with the necessary dependencies to run the application")
    private String appDependencies;   
    @Parameter(names = "-appfolder", description = "Output directory to saved generated configuration for new application")
    private String appFolder;   
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
    			Arrays.asList("",""),
    			"starter-app-1.0.jar", 
    			"../../starter-app/src/main/resources/jvms",
    			"src/test/resources", 
    			"src/test/starter-app-folder/resources", 
    			"src/test/starter-app-folder/dep", 
    			"src/test/starter-app-folder", 
    			Arrays.asList("http://localhost:81/"),
    			false, 
    			false);
    }
}