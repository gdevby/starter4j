package desktop.starter;

import com.google.gson.Gson;
import desktop.starter.component.Starter;
import desktop.starter.config.ConfigManager;
import desktop.starter.model.AppConfig;
import desktop.starter.settings.SettingsManager;
import desktop.starter.util.DesktopUtil;
import desktop.starter.util.OSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;

public class Main {
    static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    /*
    todo reuse args to run and
     */
    public static void main(String[] args) {
        try {
            //todo add special util args4j and parse args and properties and union them

            Gson g = new Gson();
            AppConfig a  = g.fromJson(new InputStreamReader(Main.class.getResourceAsStream("/settings.json"))
                    ,AppConfig.class);
            /*AppConfig a = ConfigUtil.generateTest(p.getProperty("app.name","starter"));
//
            LOGGER.info(g.toJson(a));*/
            LOGGER.info("working directory: " + DesktopUtil.getSystemPath(OSInfo.getOSType(),a.getAppName()));
//            new OSExecutorFactoryMethod().createOsExecutor().execute();

            //read settings.json
            new SettingsManager();

            //load config from web
            ConfigManager cfg = new ConfigManager();
            cfg.load();
            cfg.parse();

            Starter s = new Starter();
            //union two lines in separated class. This is more common then validate and prepare resources
            s.collectOSInfo();


            s.checkCommonProblems();
            //
            s.validate();
            s.prepareResources();
            //validate prepared resources
            s.validate();
            s.runApp();
            //wait switch off command to be sure
            //send log with error
        } catch (Throwable t) {
            //send error log on server. This is safely. We want to improve our desktop.starter.starter.
            //todo need to develop log consumer i have. i config on my server
            t.printStackTrace();
            System.exit(-1);
        }
    }
}
