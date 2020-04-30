package desktop.starter.model;

import desktop.starter.util.OSInfo;
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
    private String comment = "_comment : Config file for desktop-starter example";
    private String appName;
    private String mainClass;
    private String jvmArgs;
    private String appArgs;
    /**
     * images and other files are used an app
     */
    private List<Repo> resources;
    //used for -cp
    private List<Repo> dependencies;
}
