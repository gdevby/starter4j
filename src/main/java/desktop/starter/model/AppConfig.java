package desktop.starter.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Updating for every version.
 */
@Data
//todo put config file in git and use from git (RAW button) ,parse with gson like one object
//todo create real example to run test.jar and add new module with test and description
public class AppConfig {

    private String mainClass;
    private String jvmArgs;
    private String appArgs;

    private Map<OSInfo.OSType,String> defaultAppDirectories;
    /**
     * images and other files are used an app
     */
    private List<Repo> resources;
    //used for -cp
    private List<Repo> dependencies;
}
