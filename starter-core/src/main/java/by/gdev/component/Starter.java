package by.gdev.component;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
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
import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.handler.AccesHandler;
import by.gdev.http.download.handler.PostHandlerImpl;
import by.gdev.http.download.handler.SimvolicLinkHandler;
import by.gdev.http.download.impl.DownloaderImpl;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.Downloader;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.upload.download.downloader.DownloaderContainer;
import by.gdev.model.AppConfig;
import by.gdev.model.AppLocalConfig;
import by.gdev.model.JVMConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.process.JavaProcessHelper;
import by.gdev.subscruber.ConsoleSubscriber;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.ui.UpdateFrame;
import by.gdev.ui.subscriber.ViewSubscriber;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.StringVersionComparator;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;
import lombok.extern.slf4j.Slf4j;

/**
 * This class prepares information about the OS, validates the directories necessary for downloading, adds it to the download container and starts downloading files
 * 
 * I want to see all possible implementations and idea. So we can implement
 * upper abstraction with system.out messages!
 * 
 * @author Robert Makrytski
 */
@Slf4j
public class Starter {
	private EventBus eventBus;
	private StarterAppConfig starterConfig;
	private OSType osType;
	private Arch osArc;
	private AppConfig remoteAppConfig;
	private Repo java;
	private Repo fileRepo;
	private Repo dependencis;
	private StarterStatusFrame starterStatusFrame;
	private ResourceBundle bundle;
	private GsonService gsonService;
	private RequestConfig requestConfig;
	private HttpClientConfig httpConfig;
	private FileMapperService fileMapperService;
	private String workDir;

	public Starter(EventBus eventBus, StarterAppConfig starterConfig, ResourceBundle bundle) {
		this.eventBus = eventBus;
		this.bundle = bundle;
		this.starterConfig = starterConfig;
		httpConfig = new HttpClientConfig();
		requestConfig = RequestConfig.custom().setConnectTimeout(starterConfig.getConnectTimeout()).setSocketTimeout(starterConfig.getSocketTimeout()).build();
		fileMapperService = new FileMapperService(Main.GSON, Main.charset,starterConfig.getWorkDirectory());
		int maxAttepmts = DesktopUtil.numberOfAttempts(starterConfig.getUrlConnection(), starterConfig.getMaxAttempts(),requestConfig, httpConfig.getInstanceHttpClient());
		log.trace("Max attempts from download = " + maxAttepmts);
		HttpService httpService = new HttpServiceImpl(null, httpConfig.getInstanceHttpClient(), requestConfig,maxAttepmts);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, Main.GSON, Main.charset, starterConfig.getCacheDirectory(), starterConfig.getTimeToLife());
		gsonService = new GsonServiceImpl(Main.GSON, fileService);
	}

	/**
	 * Get information about current OS
	 */
	public void collectOSInfoAndRegisterSubscriber() {
		osType = OSInfo.getOSType();
		osArc = OSInfo.getJavaBit();
		if (!GraphicsEnvironment.isHeadless()) {
			starterStatusFrame = new StarterStatusFrame(osType, "get installed app name", true,
					ResourceBundle.getBundle("application", new Localise().getLocal()));
			eventBus.register(starterStatusFrame);
			eventBus.register(new ViewSubscriber(starterStatusFrame, bundle, osType));
			eventBus.register(new ConsoleSubscriber(bundle, fileMapperService));
			starterStatusFrame.setVisible(true);
		}
	}

	/**
	 * Validate files,java and return what we need to download
	 */
	public void validateEnvironmentAndAppRequirements() throws Exception {
		workDir = starterConfig.workDir(starterConfig.getWorkDirectory(), osType).concat("/");
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new ValidatedPartionSize(starterConfig.getMinMemorySize(),
//				new File(starterConfig.workDir(starterConfig.getWorkDirectory(), osType)), bundle));
				new File(workDir), bundle));
		validateEnvironment.add(new ValidateWorkDir(workDir, bundle));
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
		DesktopUtil.activeDoubleDownloadingResourcesLock(starterConfig.getWorkDirectory());
		DesktopUtil.activeDoubleDownloadingResourcesLock(workDir);
		Downloader downloader = new DownloaderImpl(eventBus, httpConfig.getInstanceHttpClient(), requestConfig);
		DownloaderContainer container = new DownloaderContainer();
		remoteAppConfig = gsonService.getObject(starterConfig.getServerFileConfig(starterConfig, null), AppConfig.class,false);
		updateApp(gsonService, fileMapperService);
		fileRepo = remoteAppConfig.getAppFileRepo();
		dependencis = gsonService.getObjectByUrls(remoteAppConfig.getAppDependencies().getRepositories(), remoteAppConfig.getAppDependencies().getResources().get(0).getRelativeUrl(), Repo.class, false);
		Repo resources = gsonService.getObjectByUrls(remoteAppConfig.getAppResources().getRepositories(), remoteAppConfig.getAppResources().getResources().get(0).getRelativeUrl(), Repo.class, false);
		JVMConfig jvm = gsonService.getObjectByUrls(remoteAppConfig.getJavaRepo().getRepositories(), remoteAppConfig.getJavaRepo().getResources().get(0).getRelativeUrl(), JVMConfig.class, false);
		String jvmPath = jvm.getJvms().get(osType).get(osArc).get("jre_default").getResources().get(0).getRelativeUrl();
		List<String> jvmDomain = jvm.getJvms().get(osType).get(osArc).get("jre_default").getRepositories();
		java = gsonService.getObjectByUrls(jvmDomain, jvmPath, Repo.class, false);
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
			container.filterNotExistResoursesAndSetRepo(repo, workDir);
			container.setDestinationRepositories(workDir);
			container.setHandlers(Arrays.asList(postHandler, accesHandler, linkHandler));
			downloader.addContainer(container);
		}
		downloader.startDownload(true);
		DesktopUtil.diactivateDoubleDownloadingResourcesLock();
		log.info("loading is complete");
	}

	private void updateApp(GsonService gsonService, FileMapperService fileMapperService)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		AppLocalConfig appLocalConfig;
		try {
			appLocalConfig = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG, AppLocalConfig.class);
		} catch (Exception e) {
			appLocalConfig = new AppLocalConfig();
			appLocalConfig.setCurrentAppVersion(remoteAppConfig.getAppVersion());
			fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
		}
		StringVersionComparator versionComparator = new StringVersionComparator();
		if (versionComparator.compare(appLocalConfig.getCurrentAppVersion(), remoteAppConfig.getAppVersion()) == -1) {
			if (!GraphicsEnvironment.isHeadless()) {
				// used old config without update
				if (appLocalConfig.isSkippedVersion(remoteAppConfig.getAppVersion())) {

					remoteAppConfig = gsonService.getObject(
							starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
							AppConfig.class, false);
				} else {
					UpdateFrame frame = new UpdateFrame(starterStatusFrame, bundle, appLocalConfig, remoteAppConfig,
							starterConfig, fileMapperService, osType);
					if (frame.getUserChoose() == 1) {
						remoteAppConfig = gsonService.getObject(
								starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
								AppConfig.class, true);
					} else {
						appLocalConfig.setCurrentAppVersion(remoteAppConfig.getAppVersion());
						fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
					}
				}

			}
		}
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
		Path jre = Paths.get(workDir + DesktopUtil.getJavaRun(java));
		JavaProcessHelper javaProcess = new JavaProcessHelper(String.valueOf(jre),new File(workDir), eventBus);
		String classPath = DesktopUtil.convertListToString(File.pathSeparator,javaProcess.librariesForRunning(workDir, fileRepo, dependencis));
		javaProcess.addCommands(remoteAppConfig.getJvmArguments());
		javaProcess.addCommand("-cp", classPath);
		javaProcess.addCommand(remoteAppConfig.getMainClass());
		javaProcess.start();
		if (starterConfig.isStop()) {
			javaProcess.destroyProcess();
			starterStatusFrame.setVisible(false);
		}
	}
}