package by.gdev.generator.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import by.gdev.util.model.ArgumentType;

@Data
public class AppConfigModel {
	@Parameter(names = "-name", description = "")
    private String appName;	
	@Parameter(names = "-version", description = "")
    private double appVersion;	
	@Parameter(names = "-class", description = "")
    private String mainClass;   
	@DynamicParameter(names = "-arguments", description = "")
    private Map<ArgumentType,List<String>> arguments = new HashMap<ArgumentType, List<String>>();    
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
    @Parameter(names = "-flag", description = "")
    private boolean generetedJava;
    
    @Parameter(names = "-help", help = true)
    public boolean help = false;

}
