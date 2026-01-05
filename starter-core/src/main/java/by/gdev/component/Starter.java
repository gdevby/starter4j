package by.gdev.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javafx.application.Platform;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.client.config.RequestConfig;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import by.gdev.Main;
import by.gdev.handler.UpdateCore;
import by.gdev.handler.ValidateEnvironment;
import by.gdev.handler.ValidateTempDir;
import by.gdev.handler.ValidateTempNull;
import by.gdev.handler.ValidateUpdate;
import by.gdev.handler.ValidateWorkDir;
import by.gdev.handler.ValidatedPartionSize;
import by.gdev.http.download.exeption.HashSumAndSizeError;
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
import by.gdev.model.ExceptionMessage;
import by.gdev.model.JVMConfig;
import by.gdev.model.StarterAppConfig;
import by.gdev.process.JavaProcessHelper;
import by.gdev.ui.StarterStatusStage;
import by.gdev.ui.UpdateStage;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.StringVersionComparator;
import by.gdev.util.model.InternetServerMap;
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
	private StarterStatusStage starterStatusStage;
	private ResourceBundle bundle;
	private GsonService gsonService;
	private RequestConfig requestConfig;
	@Getter
	private FileMapperService fileMapperService;
	private FileCacheService fileService;
	private String workDir;
	private UpdateCore updateCore;
	private AppLocalConfig appLocalConfig;
	private JvmRepo java;
	private InternetServerMap domainAvailability;

	public Starter(EventBus eventBus, StarterAppConfig starterConfig, ResourceBundle bundle, StarterStatusStage stage)
			throws UnsupportedOperationException, IOException, InterruptedException {
		osType = OSInfo.getOSType();
		osArc = OSInfo.getJavaBit();
		this.eventBus = eventBus;
		starterStatusStage = stage;
		this.bundle = bundle;
		this.starterConfig = starterConfig;
		requestConfig = RequestConfig.custom().setConnectTimeout(starterConfig.getConnectTimeout())
				.setSocketTimeout(starterConfig.getSocketTimeout()).build();
		fileMapperService = new FileMapperService(Main.GSON, Main.charset, starterConfig.getWorkDirectory());
		domainAvailability = DesktopUtil.testServers(starterConfig.getTestURLs(), Main.client);
		if (domainAvailability.hasInternet()) {
			domainAvailability.setMaxAttemps(starterConfig.getMaxAttempts());
		}
		log.trace("Max attempts from download = {}", domainAvailability.getMaxAttemps());
		HttpService httpService = new HttpServiceImpl(null, Main.client, domainAvailability);
		fileService = new FileCacheServiceImpl(httpService, Main.GSON, Main.charset,
				Paths.get(starterConfig.getWorkDirectory(), "cache"), starterConfig.getTimeToLife(),
				domainAvailability);
		gsonService = new GsonServiceImpl(Main.GSON, fileService, httpService, domainAvailability);
		updateCore = new UpdateCore(bundle, gsonService, fileService, starterConfig, domainAvailability);
		workDir = starterConfig.getWorkDirectory();

	}

	/**
	 * Validate files,java and return what we need to download
	 */
	public void validateEnvironmentAndAppRequirements() throws Exception {
		List<ValidateEnvironment> validateEnvironment = new ArrayList<ValidateEnvironment>();
		validateEnvironment.add(new ValidatedPartionSize(starterConfig.getMinMemorySize(), new File(workDir), bundle));
		validateEnvironment.add(new ValidateWorkDir(workDir, bundle));
		validateEnvironment.add(new ValidateTempNull(bundle));
		validateEnvironment.add(new ValidateTempDir(bundle));
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
		try {
			DesktopUtil.activeDoubleDownloadingResourcesLock(workDir);
			Downloader downloader = new DownloaderImpl(eventBus, Main.client, requestConfig, domainAvailability, false);
			DownloaderContainer container = new DownloaderContainer();
			String serverFileUrn = starterConfig.getServerFileConfig(starterConfig, starterConfig.getVersion());
			Repo resources;
			if (domainAvailability.hasInternetForDomains(starterConfig.getServerFile())) {
				log.info("app remote config: {}", starterConfig.getServerFile());
				remoteAppConfig = gsonService.getObjectByUrls(starterConfig.getServerFile(), serverFileUrn,
						AppConfig.class, false);
				updateApp(gsonService, fileMapperService);
				dependencis = gsonService.getObjectByUrls(remoteAppConfig.getAppDependencies().getRepositories(),
						remoteAppConfig.getAppDependencies().getResources().get(0).getRelativeUrl(), Repo.class, false);
				resources = gsonService.getObjectByUrls(remoteAppConfig.getAppResources().getRepositories(),
						remoteAppConfig.getAppResources().getResources().get(0).getRelativeUrl(), Repo.class, false);
				jvm = gsonService.getObjectByUrls(remoteAppConfig.getJavaRepo().getRepositories(),
						remoteAppConfig.getJavaRepo().getResources().get(0).getRelativeUrl(), JVMConfig.class, false);
			} else {
				log.info("No Internet connection");
				// when user runs after updaterDelay is overdue. We need to allow run old
				// version, New version we had installed yet
				if (Objects.nonNull(appLocalConfig)) {
					remoteAppConfig = gsonService.getLocalObject(starterConfig.getServerFile(),
							starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
							AppConfig.class);
				}
				if (Objects.isNull(remoteAppConfig)) {
					remoteAppConfig = gsonService.getLocalObject(starterConfig.getServerFile(), serverFileUrn,
							AppConfig.class);
				}
				if (Objects.isNull(remoteAppConfig)) {
					eventBus.post(new ExceptionMessage(bundle.getString("net.problem")));
					Platform.runLater(() -> System.exit(-1));
				}
				Repo dep = remoteAppConfig.getAppDependencies();
				dependencis = gsonService.getLocalObject(dep.getRepositories(),
						dep.getResources().get(0).getRelativeUrl(), Repo.class);
				Repo res = remoteAppConfig.getAppResources();
				resources = gsonService.getLocalObject(res.getRepositories(),
						res.getResources().get(0).getRelativeUrl(), Repo.class);
				Repo javaRepo = remoteAppConfig.getJavaRepo();
				jvm = gsonService.getLocalObject(javaRepo.getRepositories(),
						javaRepo.getResources().get(0).getRelativeUrl(), JVMConfig.class);
			}
			try {
				appLocalConfig = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG,
						AppLocalConfig.class);
			} catch (Exception e) {
				log.error("can't read default config {}", StarterAppConfig.APP_STARTER_LOCAL_CONFIG, e);
			}
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
				container.setDestinationRepositories(workDir);
				container.setHandlers(Arrays.asList(postHandler));
				downloader.addContainer(container);
			}
			DownloaderJavaContainer jreContainer = new DownloaderJavaContainer(fileMapperService, workDir,
					DownloaderJavaContainer.JRE_CONFIG);
			ArchiveHandler archiveHandler = new ArchiveHandler(fileMapperService, DownloaderJavaContainer.JRE_CONFIG);
			jreContainer.containerAllSize(java);
			jreContainer.filterNotExistResoursesAndSetRepo(java, workDir);
			jreContainer.setDestinationRepositories(workDir);
			jreContainer.setHandlers(Arrays.asList(postHandler, archiveHandler));
			downloader.addContainer(jreContainer);
			downloader.startDownload(true);
		} finally {
			DesktopUtil.diactivateDoubleDownloadingResourcesLock();
		}
		log.info("loading is complete");
	}

	private void updateApp(GsonService gsonService, FileMapperService fileMapperService)
            throws FileNotFoundException, IOException, ExecutionException, InterruptedException {
		StringVersionComparator versionComparator = new StringVersionComparator();
		if (Objects.nonNull(appLocalConfig) && versionComparator.compare(appLocalConfig.getCurrentAppVersion(),
				remoteAppConfig.getAppVersion()) == -1) {
			// used old config without update
			if (appLocalConfig.isSkippedVersion(remoteAppConfig.getAppVersion())) {
				remoteAppConfig = gsonService.getObjectByUrls(starterConfig.getServerFile(),
						starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
						AppConfig.class, false);
			} else {
				CompletableFuture<Integer> userChoice = new CompletableFuture<>();
				Platform.runLater(() -> {
					UpdateStage stage = new UpdateStage(starterStatusStage, bundle, appLocalConfig, remoteAppConfig,
							starterConfig, fileMapperService, osType);
					userChoice.complete(stage.getUserChoose());
				});
				if (userChoice.get() == 1) {
					remoteAppConfig = gsonService.getObjectByUrls(starterConfig.getServerFile(),
							starterConfig.getServerFileConfig(starterConfig, appLocalConfig.getCurrentAppVersion()),
							AppConfig.class, true);
				} else if (userChoice.get() == 2) {
					appLocalConfig.setCurrentAppVersion(remoteAppConfig.getAppVersion());
					fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
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
		log.info("Start application {} ", workDir);
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
		fileMapperService.write(domainAvailability, StarterAppConfig.APP_STARTER_DOMAIN_AVAILABILITY);
		map.put("starterDomainAvailabilityV1",
				Paths.get(workDir, StarterAppConfig.APP_STARTER_DOMAIN_AVAILABILITY).toAbsolutePath().toString());

		File jarFile = new File(
				URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
		map.put("starterJVM", DesktopUtil.getJavaPathByHome(true));
		map.put("starterWorkingDirectory", Paths.get("").toFile().getAbsolutePath());
		map.put("starterJarFile", jarFile.getAbsolutePath());
		map.put("starterFileEncoding", Charset.defaultCharset().toString());

		StringSubstitutor substitutor = new StringSubstitutor(map);
		javaProcess.addCommands(remoteAppConfig.getAppArguments().stream().map(s -> substitutor.replace(s))
				.collect(Collectors.toList()));
		javaProcess.start();
		if (starterConfig.isStop()) {
			javaProcess.destroyProcess();
			Platform.exit();
		}
	}

	public void updateApplication() {
		try {
			updateCore.checkUpdates(osType);
		} catch (HashSumAndSizeError t1) {
			String s = String.format(bundle.getString("upload.error.hash.sum"), t1.getUri(), t1.getLocalPath());
			eventBus.post(new ExceptionMessage(s, t1.getUri()));
		} catch (Exception e) {
			log.error("promlem with update application ", e);
		}
	}

	public void cleanCache() {
		try {
			appLocalConfig = fileMapperService.read(StarterAppConfig.APP_STARTER_LOCAL_CONFIG, AppLocalConfig.class);

			if (appLocalConfig == null) {
				return;
			}

			String lastCacheCleaningDate = appLocalConfig.getLastCacheCleaningDate();
			int cleaningInterval = starterConfig.getCleaningOldCacheFiles();
			LocalDate cleaningDate = null;

			if (lastCacheCleaningDate != null) {
				cleaningDate = LocalDate.parse(lastCacheCleaningDate).plusDays(cleaningInterval - 1);
			}

			if (Objects.isNull(cleaningDate) || LocalDate.now().isAfter(cleaningDate)) {
				fileService.cleanOldCache();
				appLocalConfig.setLastCacheCleaningDate(LocalDate.now().toString());
				fileMapperService.write(appLocalConfig, StarterAppConfig.APP_STARTER_LOCAL_CONFIG);
			}
		} catch (IOException e) {
            log.error("clean cache failed", e);
        }
    }
}