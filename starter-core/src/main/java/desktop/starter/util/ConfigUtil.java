package desktop.starter.util;

import desktop.starter.model.AppConfig;
import desktop.starter.model.Metadata;
import desktop.starter.model.Repo;

import java.util.*;

public class ConfigUtil {
    public static AppConfig generateTest(String appName) {
        AppConfig a = new AppConfig();
        a.setMainClass("desktop.starter.app.Main");
        Map<OSInfo.OSType, String> defaultFolders = new HashMap<>();
        a.setAppName("starter");
        List<Repo> list = new ArrayList<>();
        Repo r = new Repo();
        r.setRepo(Collections.singletonList("https://repo1.maven.org/maven2"));
        Metadata m = new Metadata();
        m.setRelUrl("/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar");
        m.setPath("/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar");
        m.setSize(290339);
        m.setSha1("7c4f3c474fb2c041d8028740440937705ebb473a");
        r.setResources(Collections.singletonList(m));
        list.add(r);



        r = new Repo();
        m = new Metadata();
        m.setUrls(Collections.singletonList("https://raw.githubusercontent.com/robertmakrytski/starter-app/master/apps/original-starter-app-1.0.jar"));
        m.setPath("/starter-app/master/apps/original-starter-app-1.0.jar");
        m.setSize(57830);
        m.setSha1("0bed0b7e14cbdd9e383714d5c3aa67a59eaa5311");
        r.setResources(Collections.singletonList(m));
        list.add(r);
        a.setDependencies(list);
        return a;
    }
}
