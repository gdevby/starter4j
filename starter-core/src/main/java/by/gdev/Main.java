package by.gdev;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;

import com.beust.jcommander.JCommander;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import by.gdev.component.Bootstrapper;
import by.gdev.handler.Localise;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateFont;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateUpdate;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.handler.ValidatedPartionSize;
import by.gdev.http.head.cache.config.HttpConfig;
import by.gdev.http.head.cache.handler.AccesHandler;
import by.gdev.http.head.cache.handler.PostHandlerImpl;
import by.gdev.http.head.cache.impl.DownloaderImpl;
import by.gdev.http.head.cache.impl.FileCacheServiceImpl;
import by.gdev.http.head.cache.impl.GsonServiceImpl;
import by.gdev.http.head.cache.impl.HttpServiceImpl;
import by.gdev.http.head.cache.model.downloader.DownloaderContainer;
import by.gdev.http.head.cache.service.Downloader;
import by.gdev.http.head.cache.service.FileCacheService;
import by.gdev.http.head.cache.service.GsonService;
import by.gdev.http.head.cache.service.HttpService;
import by.gdev.model.AppConfig;
import by.gdev.model.JVMConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.process.JavaProcessHelper;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.model.download.Repo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static Charset charset = StandardCharsets.UTF_8;
	//TODO split to starter
	public static void main(String[] args) throws Exception {
		boolean flag = true;
		System.setProperty("java.net.preferIPv4Stack", String.valueOf(flag));
		ConsoleSubscriber listener = new ConsoleSubscriber();
		DesktopUtil desktopUtil = new DesktopUtil();
		StarterAppConfig starterConfig = StarterAppConfig.DEFAULT_CONFIG;
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
				log.error(bundle.getString("validate.error") + " " + val.getClass().getName());
				eventBus.post(val.getExceptionMessage());
			} else {
				log.debug(bundle.getString("validate.successful") + " " + val.getClass().getName());
			}
		}
		OSInfo.OSType osType = OSInfo.getOSType();
		Arch osArc = OSInfo.getJavaBit();
		HttpConfig httpConfig = new HttpConfig();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(2000).build();
		int maxAttepmts = DesktopUtil.init(4, requestConfig, httpConfig.httpClient());
		HttpService httpService = new HttpServiceImpl(null, httpConfig.httpClient(), requestConfig, maxAttepmts);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, gson, charset, Paths.get("target"), 600000);
		GsonService gsonService = new GsonServiceImpl(gson, fileService);
		Downloader downloader = new DownloaderImpl(eventBus, httpConfig.httpClient(), requestConfig);
		DownloaderContainer container = new DownloaderContainer();
		AppConfig all = gsonService.getObject(starterConfig.controlVersion(starterConfig), AppConfig.class, false);
		Repo fileRepo = all.getAppFileRepo();
		Repo dependencis = gsonService.getObject(all.getAppDependencies().getRepositories().get(0) + all.getAppDependencies().getResources().get(0).getRelativeUrl(), Repo.class, false);
		Repo resources = gsonService.getObject(all.getAppResources().getRepositories().get(0) + all.getAppResources().getResources().get(0).getRelativeUrl(), Repo.class, false);
		JVMConfig jvm = gsonService.getObject(all.getJavaRepo().getRepositories().get(0) + all.getJavaRepo().getResources().get(0).getRelativeUrl(), JVMConfig.class, false);
		String jvmPath = jvm.getJvms().get(osType).get(osArc).get("jre_default").getResources().get(0).getRelativeUrl();
		String jvmDomain = jvm.getJvms().get(osType).get(osArc).get("jre_default").getRepositories().get(0);	
		Repo java = gsonService.getObject(jvmDomain + jvmPath, Repo.class, false);
		List<Repo> list = new ArrayList<Repo>();
		list.add(resources);
		list.add(dependencis);
		list.add(fileRepo);
		list.add(java);
		PostHandlerImpl postHandler = new PostHandlerImpl();
		AccesHandler accesHandler = new AccesHandler();
		for (Repo repo : list) {
			container.setDestinationRepositories(starterConfig.getWorkDirectory());
			container.setRepo(repo);
			container.setHandlers(Arrays.asList(postHandler, accesHandler));
			downloader.addContainer(container);
		}
		desktopUtil.activeDoubleDownloadingResourcesLock(starterConfig.getWorkDirectory());
//		downloader.startDownload(false);
		desktopUtil.diactivateDoublePreparingJVM();
		String jre = String.valueOf(DesktopUtil.getAbsolutePathToJava(osType, starterConfig.getWorkDirectory()));
		JavaProcessHelper javaProcess = new JavaProcessHelper(jre, new File(starterConfig.getWorkDirectory()), eventBus);
		String  classPath = DesktopUtil.convertListToString(File.pathSeparator, javaProcess.librariesForRunning(Paths.get(starterConfig.getWorkDirectory())));
		javaProcess.addCommand("-cp", classPath);
		javaProcess.addCommand(all.getMainClass());
		javaProcess.start();
		
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
			Bootstrapper s = new Bootstrapper();
			// union two lines in separated class. This is more common then validate and
			// prepare resources
//			s.collectOSInfo();
//			s.checkCommonProblems();
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