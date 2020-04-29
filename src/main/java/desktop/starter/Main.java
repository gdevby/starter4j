package desktop.starter;

import desktop.starter.component.Starter;
import desktop.starter.component.config.ConfigManager;
import desktop.starter.component.factory.FactoryMethod;
import desktop.starter.component.settings.SettingsManager;

public class Main {
    /*
    todo reuse args to run and
     */
    public static void main(String[] args) {
        try {
            new FactoryMethod().createOsExecutor().execute();

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
            System.out.println("Shutting down...");
            System.exit(-1);
        }
    }
}
