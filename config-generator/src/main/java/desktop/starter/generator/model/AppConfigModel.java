package desktop.starter.generator.model;

import desktop.starter.util.OSInfo;
import desktop.starter.util.model.ArgumentType;
import desktop.starter.util.model.download.Repo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AppConfigModel {
    private String appName;
    private String mainClass;
    private Map<ArgumentType,List<String>> arguments;
    private String jvms;
    private String resourcesPath;
    //used for -cp
    private String dependenciesPath;
}
