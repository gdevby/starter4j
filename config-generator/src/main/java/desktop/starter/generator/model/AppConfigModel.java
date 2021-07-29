package desktop.starter.generator.model;

import desktop.starter.util.model.ArgumentType;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AppConfigModel {
    private String appName;
    private double appVersion;
    private String mainClass;
    private Map<ArgumentType,List<String>> arguments;
    private String appFile;
    private String javaFolder;
    private String appResources;
    //used for -cp
    private String appDependencies;
    private String appFolder;
    private boolean generetedJava;
     //todo remove line lower.
//    public String h
}
