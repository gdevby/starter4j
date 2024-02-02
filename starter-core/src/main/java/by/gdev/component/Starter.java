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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.apache.http.client.config.RequestConfig;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import by.gdev.Main;
import by.gdev.handler.UpdateCore;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateFont;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateUpdate;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.handler.ValidatedPartionSize;
import by.gdev.http.download.config.HttpClientConfig;
import by.gdev.http.download.handler.ArchiveHandler;
import by.gdev.http.download.handler.PostHandlerImpl;
import by.gdev.http.download.impl.DownloaderImpl;
import by.gdev.http.download.impl.FileCacheServiceImpl;
import by.gdev.http.download.impl.GsonServiceImpl;
import by.gdev.http.download.impl.HttpServiceImpl;
import by.gdev.http.download.service.Downloader;
import by.gdev.http.download.service.FileCacheService;
import by.gdev.http.download.service.GsonService;
import by.gdev.http.download.service.HttpService;
import by.gdev.http.upload.download.downloader.DownloaderContainer;
import by.gdev.http.upload.download.downloader.DownloaderJavaContainer;
import by.gdev.model.AppConfig;
import by.gdev.model.AppLocalConfig;
import by.gdev.model.JVMConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.process.JavaProcessHelper;
import by.gdev.ui.StarterStatusFrame;
import by.gdev.ui.UpdateFrame;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.StringVersionComparator;
import by.gdev.util.model.download.JvmRepo;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class prepares information about the OS, validates the directories
 * necessary for downloading, adds it to the download container and starts
 * downloading files
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
	private JVMConfig jvm;
	private Repo dependencis;
	private StarterStatusFrame starterStatusFrame;
	private ResourceBundle bundle;
	private GsonService gsonService;
	private RequestConfig requestConfig;
	@Getter
	private FileMapperService fileMapperService;
	private String workDir;
	private boolean hasInternet;
	private UpdateCore updateCore;
	private AppLocalConfig appLocalConfig;
	private JvmRepo java;

	public Starter(EventBus eventBus, StarterAppConfig starterConfig, ResourceBundle bundle, StarterStatusFrame frame)
			throws UnsupportedOperationException, IOException, InterruptedException {
		osType = OSInfo.getOSType();
		osArc = OSInfo.getJavaBit();
		this.eventBus = eventBus;
		starterStatusFrame = frame;
		this.bundle = bundle;
		this.starterConfig = starterConfig;
		requestConfig = RequestConfig.custom().setConnectTimeout(starterConfig.getConnectTimeout())
				.setSocketTimeout(starterConfig.getSocketTimeout()).build();
		fileMapperService = new FileMapperService(Main.GSON, Main.charset, starterConfig.getWorkDirectory());
		int maxAttepmts = DesktopUtil.numberOfAttempts(starterConfig.getUrlConnection(), starterConfig.getMaxAttempts(),
				requestConfig, HttpClientConfig.getInstanceHttpClient());
		hasInternet = maxAttepmts == 1 ? false : true;
		log.trace("Max attempts from download = " + maxAttepmts);
		HttpService httpService = new HttpServiceImpl(null, HttpClientConfig.getInstanceHttpClient(), requestConfig,
				maxAttepmts);
		FileCacheService fileService = new FileCacheServiceImpl(httpService, Main.GSON, Main.charset,
				starterConfig.getCacheDirectory(), starterConfig.getTimeToLife());
		gsonService = new GsonServiceImpl(Main.GSON, fileService, httpService);
		updateCore = new UpdateCore(bundle, gsonService, HttpClientConfig.getInstanceHttpClient(), requestConfig);
	}

	/**
	 * Validate files,java and return what we need to download
	 */
	public void validateEnvironmentAndAppRequirements() throws Exception {
		workDir = starterConfig.workDir(starterConfig.getWorkDirectory(), osType).concat("/");
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new ValidatedPartionSize(starterConfig.getMinMemorySize(), new File(workDir), bundle));
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
		DesktopUtil.activeDoubleDownloadingResourcesLock(workDir);
		Downloader downloader = new DownloaderImpl(eventBus, HttpClientConfig.getInstanceHttpClient(), requestConfig);
		DownloaderContainer container = new DownloaderContainer();
		List<String> serverFile = starterConfig.getServerFileConfig(starterConfig, starterConfig.getVersion());
		Repo resources;
		if (hasInternet) {
			log.info("app remote config: {}", serverFile.toString());
			remoteAppConfig = gsonService.getObjectByUrls(serverFile, AppConfig.class, false);
			updateApp(gsonService, fileMapperService);
			dependencis = gsonService.getObjectByUrls(remoteAppConfig.getAppDependencies().getRepositories(),
					remoteAppConfig.getAppDependencies().getResources(), Repo.class, false);
			resources = gsonService.getObjectByUrls(remoteAppConfig.getAppResources().getRepositories(),
					remoteAppConfig.getAppResources().getResources(), Repo.class, false);
			jvm = gsonService.getObjectByUrls(remoteAppConfig.getJavaRepo().getRepositories(),
					remoteAppConfig.getJavaRepo().getResources(), JVMConfig.class, false);
		} else {
			log.info("No Internet connection");
			remoteAppConfig = gsonService.getLocalObject(Lists.newArrayList(serverFile), AppConfig.class);
			Repo dep = remoteAppConfig.getAppDependencies();
			List<String> d = DesktopUtil.generatePath(dep.getRepositories(), dep.getResources());
			dependencis = gsonService.getLocalObject(d, Repo.class);
			Repo res = remoteAppConfig.getAppResources();
			List<String> r = DesktopUtil.generatePath(res.getRepositories(), res.getResources());
			resources = gsonService.getLocalObject(r, Repo.class);
			Repo javaRepo = remoteAppConfig.getJavaRepo();
			List<String> j = DesktopUtil.generatePath(javaRepo.getRepositories(), javaRepo.getResources());
			jvm = gsonService.getLocalObject(j, JVMConfig.class);
		}
		appLocalConfig = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG, AppLocalConfig.class);
		if (appLocalConfig == null) {
			appLocalConfig = new AppLocalConfig();
			appLocalConfig.setCurrentAppVersion(remoteAppConfig.getAppVersion());
			fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
		}
		if (Objects.nonNull(starterConfig.getVersion())) {
			appLocalConfig.setCurrentAppVersion(starterConfig.getVersion());
			fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
		}

		Repo fileRepo = remoteAppConfig.getAppFileRepo();
		java = jvm.getJvms().get(osType).get(osArc).get(DownloaderJavaContainer.JRE_DEFAULT);
		List<Repo> list = Lists.newArrayList(fileRepo, dependencis, resources);
		PostHandlerImpl postHandler = new PostHandlerImpl();
		for (Repo repo : list) {
			container.containerAllSize(repo);
			container.filterNotExistResoursesAndSetRepo(repo, workDir);
			container.downloadSize(repo, workDir);
			container.setDestinationRepositories(workDir);
			container.setHandlers(Arrays.asList(postHandler));
			downloader.addContainer(container);
		}
		DownloaderJavaContainer jreContainer = new DownloaderJavaContainer(fileMapperService, workDir,
				DownloaderJavaContainer.JRE_CONFIG);
		ArchiveHandler archiveHandler = new ArchiveHandler(fileMapperService, DownloaderJavaContainer.JRE_CONFIG);
		jreContainer.containerAllSize(java);
		jreContainer.filterNotExistResoursesAndSetRepo(java, workDir);
		jreContainer.downloadSize(java, workDir);
		jreContainer.setDestinationRepositories(workDir);
		jreContainer.setHandlers(Arrays.asList(postHandler, archiveHandler));
		downloader.addContainer(jreContainer);
		downloader.startDownload(true);
		DesktopUtil.diactivateDoubleDownloadingResourcesLock();
		log.info("loading is complete");
	}

	private void updateApp(GsonService gsonService, FileMapperService fileMapperService)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		StringVersionComparator versionComparator = new StringVersionComparator();
		if (Objects.nonNull(appLocalConfig) && versionComparator.compare(appLocalConfig.getCurrentAppVersion(),
				remoteAppConfig.getAppVersion()) == -1) {
			if (!GraphicsEnvironment.isHeadless()) {
				// used old config without update
				if (appLocalConfig.isSkippedVersion(remoteAppConfig.getAppVersion())) {
					remoteAppConfig = gsonService.getObjectByUrls(
							starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
							AppConfig.class, false);
				} else {
					UpdateFrame frame = new UpdateFrame(starterStatusFrame, bundle, appLocalConfig, remoteAppConfig,
							starterConfig, fileMapperService, osType);
					if (frame.getUserChoose() == 1) {
						remoteAppConfig = gsonService.getObjectByUrls(
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
		Path jre = DesktopUtil
				.getJavaRun(Paths.get(workDir, DownloaderJavaContainer.JRE_DEFAULT, java.getJreDirectoryName()));
		JavaProcessHelper javaProcess = new JavaProcessHelper(String.valueOf(jre), new File(workDir), eventBus);
		String classPath = DesktopUtil.convertListToString(File.pathSeparator,
				javaProcess.librariesForRunning(workDir, remoteAppConfig.getAppFileRepo(), dependencis));
		javaProcess.addCommands(remoteAppConfig.getJvmArguments());
		javaProcess.addCommand("-cp", classPath);
		javaProcess.addCommand(remoteAppConfig.getMainClass());
		javaProcess.addCommands(remoteAppConfig.getAppArguments());
		Map<String, String> map = new HashMap<>();
		map.put("currentAppVersion", appLocalConfig.getCurrentAppVersion());
		StringSubstitutor substitutor = new StringSubstitutor(map);
		javaProcess.addCommands(remoteAppConfig.getAppArguments().stream().map(s -> substitutor.replace(s))
				.collect(Collectors.toList()));
		javaProcess.start();
		if (starterConfig.isStop()) {
			javaProcess.destroyProcess();
		}
	}

	public void updateApplication() {
		try {
			updateCore.checkUpdates(osType, starterConfig.getStarterUpdateConfig());
		} catch (Exception e) {
			log.error("promlem with update application ", e);
		}
	}
}