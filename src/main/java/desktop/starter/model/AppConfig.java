package desktop.starter.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
//todo put config file in git and use from git (RAW button) ,parse with gson like one object
//todo create real example to run test.jar and add new module with test and description
public class AppConfig {
    List<Repo> data;
    Map<OSInfo.OSType,String> defaultAppDirectories;
    String mainClass;
}
