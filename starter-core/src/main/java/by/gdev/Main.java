package by.gdev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.UnsupportedLookAndFeelException;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;

import by.gdev.component.Starter;
import by.gdev.handler.ConsoleSubscriber;
import by.gdev.handler.Localise;
import by.gdev.handler.ValidatedPartionSize;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateFont;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateUpdate;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.model.StarterAppConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main  {

	/*
	 * todo reuse args to run and
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		ConsoleSubscriber listener = new ConsoleSubscriber();
		StarterAppConfig starterConfig = new StarterAppConfig();
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		EventBus eventBus = new EventBus();
		eventBus.register(listener);
		ResourceBundle bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new ValidatedPartionSize(starterConfig.getMinMemorySize()));
		validateEnvironment.add(new ValidateWorkDir());
		validateEnvironment.add(new ValidateTempNull());
		validateEnvironment.add(new ValidateTempDir());
		validateEnvironment.add(new ValidateFont());
		validateEnvironment.add(new ValidateUpdate());
		for (ValidateEnvironment val : validateEnvironment) {
			if (!val.validate()) {
				log.error(bundle.getString("validate.error") +" "+ val.getClass().getName());
				eventBus.post(val.getExceptionMessage());
			}else {
				log.debug(bundle.getString("validate.successful") +" "+ val.getClass().getName());
			}
		}
		try {

			// todo add special util args4j and parse args and properties and union them
//			AppConfig a = g.fromJson(new InputStreamReader(Main.class.getResourceAsStream("/settings.json")),
//					AppConfig.class);
			/*
			 * AppConfig a = ConfigUtil.generateTest(p.getProperty("app.name","starter"));
			 * // LOGGER.info(g.toJson(a));
			 */
//			log.info("working directory: " + DesktopUtil.getSystemPath(OSInfo.getOSType(), a.getAppName()));
//            new OSExecutorFactoryMethod().createOsExecutor().execute();

			// read settings.json
//            new SettingsManager();!!!!!!!!!!!

			// load config from web
//            ConfigManager cfg = new ConfigManager();!!!!!!!!!!!!!!!!!!
//            cfg.load();!!!!!!!!!!!!!!!!!!
//            cfg.parse();!!!!!!!!!!!!!!!!!!

			Starter s = new Starter();
			// union two lines in separated class. This is more common then validate and
			// prepare resources
//			s.collectOSInfo();

//			s.checkCommonProblems();
			//
//			s.validate();
//			s.prepareResources();
			// validate prepared resources
//			s.validate();
			s.runApp();
			// wait switch off command to be sure
			// send log with error
		} catch (Throwable t) {
			// send error log on server. This is safely. We want to improve our
			// by.gdev.gdev.
			// todo need to develop log consumer i have. i config on my server
			t.printStackTrace();
			System.exit(-1);
		}
	}
}
