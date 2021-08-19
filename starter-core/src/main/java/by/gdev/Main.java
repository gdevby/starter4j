package by.gdev;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import by.gdev.component.Starter;
import by.gdev.handler.ConsoleSubscriber;
import by.gdev.handler.Validate;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.model.StarterAppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {
//	static Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	/*
	 * todo reuse args to run and
	 */
	public static void main(String[] args) throws IOException {		
		ConsoleSubscriber listener = new ConsoleSubscriber();
		StarterAppConfig starterConfig = new StarterAppConfig();
		JCommander.newBuilder().addObject(starterConfig).build().parse(args);
		EventBus eventBus = new EventBus();
		eventBus.register(listener);	
		
		Files.createTempFile("test", "txt");
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new Validate());
		validateEnvironment.add(new ValidateWorkDir());
		validateEnvironment.add(new ValidateTempNull());
		validateEnvironment.add(new ValidateTempDir());
		for(ValidateEnvironment validate : validateEnvironment) {
			if (!validate.valite()) {
				eventBus.post(validate.getExceptionMessage());
			}
		}
		try {
			
			// todo add special util args4j and parse args and properties and union them

			Gson g = new Gson();
//			AppConfig a = g.fromJson(new InputStreamReader(Main.class.getResourceAsStream("/settings.json")),
//					AppConfig.class);
			/*
			 AppConfig a = ConfigUtil.generateTest(p.getProperty("app.name","starter"));
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
			s.collectOSInfo();

			s.checkCommonProblems();
			//
			s.validate();
			s.prepareResources();
			// validate prepared resources
			s.validate();
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
