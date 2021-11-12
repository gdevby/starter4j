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
	@Parameter(names = "-name", description = "")
    private String appName;	
	@Parameter(names = "-version", description = "")
    private double appVersion;	
	@Parameter(names = "-class", description = "")
    private String mainClass;   	
	@Parameter(names = "-app", description = "")
    private List<String> appArguments; 	
	@Parameter(names = "-jvm", description = "")
	 private List<String> jvmArguments;	
    @Parameter(names = "-file", description = "")
    private String appFile;    
    @Parameter(names = "-javafolder", description = "")
    private String javaFolder;   
    @Parameter(names = "-config", description = "")
    private String javaConfig;    
    @Parameter(names = "-resources", description = "")
    private String appResources;   
    //used for -cp
    @Parameter(names = "-dependencies", description = "")
    private String appDependencies;   
    @Parameter(names = "-appfolder", description = "")
    private String appFolder;   
    @Parameter(names = "-domain", description = "")
    private List<String> domain;  
    @Parameter(names = "-flag", description = "")
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
    			"../starter-app/src/main/resources/jvms",
    			"src/test/resources", 
    			"src/test/starter-app-folder/resources", 
    			"src/test/starter-app-folder/dep", 
    			"src/test/starter-app-folder", 
    			Arrays.asList("http://localhost:81/"),
    			false, 
    			false);
    }
}