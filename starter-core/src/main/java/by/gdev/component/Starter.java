package by.gdev.component;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.client.config.RequestConfig;

import com.google.common.eventbus.EventBus;

import by.gdev.Main;
import by.gdev.handler.Localise;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateFont;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateUpdate;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.handler.ValidatedPartionSize;
import by.gdev.http.head.cache.config.HttpClientConfig;
import by.gdev.http.head.cache.handler.AccesHandler;
import by.gdev.http.head.cache.handler.PostHandlerImpl;
import by.gdev.http.head.cache.handler.SimvolicLinkHandler;
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
import by.gdev.process.JavaProcess;
import by.gdev.process.JavaProcessHelper;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.ui.ValidatorMessageSubscriber;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Repo;
import lombok.extern.slf4j.Slf4j;

/**
 * I want to see all possible implementations and idea. So we can implement
 * upper abstraction with system.out messages!
 */
@Slf4j
public class Starter {
	private EventBus eventBus;
	private StarterAppConfig starterConfig;
	private OSType osType;
	private Arch osArc;
	private AppConfig all;
	private Repo java;
	private Repo fileRepo;
	private Repo dependencis;
	JavaProcess procces;
	private StarterStatusFrame starterStatusFrame;

	public Starter(EventBus eventBus, StarterAppConfig starterConfig) {
		this.eventBus = eventBus;
		this.starterConfig = starterConfig;
	}

	/**
	 * Get information about current OS
	 */
	public void collectOSInfo() {
		osType = OSInfo.getOSType();
		osArc = OSInfo.getJavaBit();
		if (!GraphicsEnvironment.isHeadless()) {
			starterStatusFrame = new StarterStatusFrame(osType, "get installed app name", true,
					ResourceBundle.getBundle("application", new Localise().getLocal()));
			eventBus.register(starterStatusFrame);
			eventBus.register(new ValidatorMessageSubscriber(starterStatusFrame));
			starterStatusFrame.setVisible(true);
		}
	}

	// TODO aleksandr to delete
	public void checkCommonProblems() {
		log.info("call method {}", "checkCommonProblems");
	}

	/**
	 * Validate files,java and return what we need to download
	 */
	public void validateEnvironmentAndAppRequirements() throws Exception {
		ResourceBundle bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new ValidatedPartionSize(starterConfig.getMinMemorySize(),	new File(starterConfig.workDir(starterConfig.getWorkDirectory())), bundle));
		validateEnvironment.add(new ValidateWorkDir(starterConfig.workDir(starterConfig.getWorkDirectory()), bundle));
		validateEnvironment.add(new ValidateTempNull(bundle));
		validateEnvironment.add(new ValidateTempDir(bundle));
		validateEnvironment.add(new ValidateFont(bundle));
		validateEnvironment.add(new ValidateUpdate(bundle));
		for (ValidateEnvironment val : validateEnvironment) {
			if (!val.validate()) {
				log.error(bundle.getString("validate.error") + " " + val.getClass().getName());
				eventBus.post(val.getExceptionMessage());
			} else {
				log.debug(bundle.getString("validate.successful") + " " + val.getClass().getName());
			}
		}
	}

	/**
	 * Download resources(java,files,.jar) and and prepare them to use, after this
	 * we need revalidate again
	 */
	public void prepareResources() throws Exception {
		log.info("Start loading");
		log.info(String.valueOf(osType));
		log.info(String.valueOf(osArc));
		DesktopUtil desktopUtil = new DesktopUtil();
		desktopUtil.activeDoubleDownloadingResourcesLock(starterConfig.getWorkDirectory());
		HttpClientConfig httpConfig = new HttpClientConfig();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		int maxAttepmts = DesktopUtil.numberOfAttempts(starterConfig.getUrlConnection(), 4, requestConfig,
				httpConfig.getInstanceHttpClient());
		HttpService httpService = new HttpServiceImpl(null, httpConfig.getInstanceHttpClient(), requestConfig,
				maxAttepmts);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, Main.GSON, Main.charset,
				Paths.get("target/out", "config"), 600000);
		GsonService gsonService = new GsonServiceImpl(Main.GSON, fileService);
		Downloader downloader = new DownloaderImpl(eventBus, httpConfig.getInstanceHttpClient(), requestConfig);
		DownloaderContainer container = new DownloaderContainer();
		all = gsonService.getObject(starterConfig.getServerFileConifg(starterConfig), AppConfig.class, false);
		fileRepo = all.getAppFileRepo();
		dependencis = gsonService.getObject(all.getAppDependencies().getRepositories().get(0)+ all.getAppDependencies().getResources().get(0).getRelativeUrl(), Repo.class, false);
		Repo resources = gsonService.getObject(all.getAppResources().getRepositories().get(0)+ all.getAppResources().getResources().get(0).getRelativeUrl(), Repo.class, false);
		JVMConfig jvm = gsonService.getObject(all.getJavaRepo().getRepositories().get(0) + all.getJavaRepo().getResources().get(0).getRelativeUrl(),JVMConfig.class, false);
		String jvmPath = jvm.getJvms().get(osType).get(osArc).get("jre_default").getResources().get(0).getRelativeUrl();
		String jvmDomain = jvm.getJvms().get(osType).get(osArc).get("jre_default").getRepositories().get(0);
		java = gsonService.getObject(jvmDomain + jvmPath, Repo.class, false);
		List<Repo> list = new ArrayList<Repo>();
		list.add(fileRepo);
		list.add(dependencis);
		list.add(resources);
		list.add(java);
		PostHandlerImpl postHandler = new PostHandlerImpl();
		AccesHandler accesHandler = new AccesHandler();
		SimvolicLinkHandler linkHandler = new SimvolicLinkHandler();
		for (Repo repo : list) {
			container.conteinerAllSize(repo);
			container.filterNotExistResoursesAndSetRepo(repo, starterConfig.getWorkDirectory());
			container.setDestinationRepositories(starterConfig.getWorkDirectory());
			container.setHandlers(Arrays.asList(postHandler, accesHandler, linkHandler));
			downloader.addContainer(container);
		}
		downloader.startDownload(true);
		desktopUtil.diactivateDoubleDownloadingResourcesLock();
		log.info("loading is complete");
	}

	/**
	 * Run app and wait some command to switch off , cas we run in new process
	 * switch off command 'Starter run app'
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void runApp() throws IOException, InterruptedException {
		log.info("Start application");
		Path jre = Paths.get(starterConfig.getWorkDirectory() + DesktopUtil.getJavaRun(java)).toAbsolutePath();
		JavaProcessHelper javaProcess = new JavaProcessHelper(String.valueOf(jre),new File(starterConfig.getWorkDirectory()), eventBus);
		String classPath = DesktopUtil.convertListToString(File.pathSeparator,javaProcess.librariesForRunning(starterConfig.getWorkDirectory(), fileRepo, dependencis));
		javaProcess.addCommands(all.getJvmArguments());
		javaProcess.addCommand("-cp", classPath);
		javaProcess.addCommand(all.getMainClass());
		procces = javaProcess.start();
		if (starterConfig.isStop()) {
			Thread.sleep(600);
			procces.getProcess().destroy();
		}
	}
}
