package desktop.starter.generator;

import desktop.starter.model.Repo;
import desktop.starter.util.OSInfo;
import desktop.starter.util.model.ArgumentType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AppConfigModel {
    private String appName;
    private String mainClass;
    private Map<ArgumentType,List<String>> arguments;
    private Map<OSInfo.OSType, Repo> jvms;
    private String resourcesPath;
    //used for -cp
    private String dependenciesPath;
}
