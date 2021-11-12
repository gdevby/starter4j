package by.gdev.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import by.gdev.generator.model.AppConfigModel;
import by.gdev.model.AppConfig;
import by.gdev.model.JVMConfig;
import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import by.gdev.util.OSInfo.Arch;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;

public class AppConfigCreator {
	public static final String APP_CONFIG_GENERATOR = "appConfigModel.json";
	public static final String DOMAIN_CONFIG = "domainConfig.json";
	public static final String TEMP_APP_CONFIG = "tempAppConfig.json";
	public static final String TARGET_OUT_FOLDER = "target/out";
	public static final String APP_DEPENDENCISES_CONFIG = "dependencises.json";
	public static final String APP_RESOURCES_CONFIG = "resources.json";
	public static final String JAVA_CONFIG = "javaConfig.json";
	FileMapperService fileMapperService;
	/**
	 * @param configFile contains config app
	 * @param fms
	 * @return generated AppConfig
	 * @throws NoSuchAlgorithmException
	 */
	public AppConfigCreator(FileMapperService fileMapperService) {
		this.fileMapperService = fileMapperService;
	}

	public AppConfig createConfig(AppConfigModel configFile)
			throws IOException, NoSuchAlgorithmException {
		AppConfig appConfig = new AppConfig();
		String version = Paths.get(configFile.getAppName(), String.valueOf(configFile.getAppVersion())).toString();
		Path appFolder = Paths.get(configFile.getAppFolder());
		Path dependencies = Paths.get(configFile.getAppDependencies());
		Path resources = Paths.get(configFile.getAppResources());
		Path dependenciesConfig = Paths.get(TARGET_OUT_FOLDER, version, APP_DEPENDENCISES_CONFIG);
		Path resourcesConfig = Paths.get(TARGET_OUT_FOLDER, version, APP_RESOURCES_CONFIG);
		FileUtils.copyDirectory(appFolder.toFile(), Paths.get(TARGET_OUT_FOLDER, version).toFile());
		appConfig.setAppName(configFile.getAppName());
		appConfig.setAppVersion(configFile.getAppVersion());
		appConfig.setAppArguments(configFile.getAppArguments());
		appConfig.setJvmArguments(configFile.getJvmArguments());	
		appConfig.setMainClass(configFile.getMainClass());
		appConfig.setAppFileRepo(createRepo(appFolder, Paths.get(configFile.getAppFolder(), configFile.getAppFile()),
				configFile.getAppName(), configFile));
		fileMapperService.write(createJreConfig(configFile), Paths.get(TARGET_OUT_FOLDER, JAVA_CONFIG).toString());
		fileMapperService.write(createRepo(dependencies, dependencies, 
				Paths.get(version, dependencies.getFileName().toString()).toString(), configFile), dependenciesConfig.toString());
		fileMapperService.write(createRepo(resources, resources, 
				Paths.get(version, resources.getFileName().toString()).toString(), configFile), resourcesConfig.toString());
		appConfig.setAppDependencies(
				createRepo(Paths.get(TARGET_OUT_FOLDER, version), dependenciesConfig, version, configFile));
		appConfig.setAppResources(createRepo(Paths.get(TARGET_OUT_FOLDER, version), resourcesConfig, version, configFile));
		if (configFile.isGeneretedJava()) {
			createJreConfig(configFile);
			appConfig.setJavaRepo(createRepo(Paths.get(TARGET_OUT_FOLDER), Paths.get(TARGET_OUT_FOLDER, JAVA_CONFIG),
					Paths.get(configFile.getAppName()).toString(), configFile));
		}else {
			AppConfig app = fileMapperService.read(Paths.get(configFile.getJavaConfig(), TEMP_APP_CONFIG).toString(), AppConfig.class);
			appConfig.setJavaRepo(app.getJavaRepo());
		}
		return appConfig;
	}

	private Repo createRepo(Path jvms, Path folder, String str, AppConfigModel configFile) throws IOException {
		List<Metadata> metadataList = Files.walk(folder).filter(Files::isRegularFile).map(DesktopUtil.wrap(e -> {
			Path s = jvms.relativize(e);
			Metadata m = new Metadata();
			m.setSha1(DesktopUtil.getChecksum(e.toFile(), "SHA-1"));
			m.setPath(s.toString());
			m.setSize(e.toFile().length());
			if (Objects.nonNull(str)) {
				s = Paths.get(str, s.toString());
			}
			m.setRelativeUrl(s.toString());
			return m;
		})).collect(Collectors.toList());
		Repo r = new Repo();
		r.setResources(metadataList);
		r.setRepositories(configFile.getDomain());
		return r;
	}

	private List<Path> listPath(Path p) throws IOException {
		return Files.walk(p, 1).filter(entry -> !entry.equals(p)).collect(Collectors.toList());
	}

	JVMConfig createJreConfig(AppConfigModel configFile)
			throws IOException, NoSuchAlgorithmException {
		JVMConfig jvm = new JVMConfig();
		jvm.setJvms(new HashMap<OSInfo.OSType, Map<Arch, Map<String, Repo>>>());
		for (Path pathTypeOS : listPath(Paths.get(configFile.getJavaFolder()))) {
			OSType type = OSType.valueOf(pathTypeOS.getFileName().toString().toUpperCase(Locale.ROOT));
			jvm.getJvms().put(type, new HashMap<OSInfo.Arch, Map<String, Repo>>());
			for (Path pathArch : listPath(pathTypeOS)) {
				Arch arch = Arch.valueOf(pathArch.getFileName().toString().toLowerCase(Locale.ROOT));
				jvm.getJvms().get(type).put(arch, new HashMap<String, Repo>());
				for (Path pathKey : listPath(pathArch)) {
					String key = String.valueOf(pathKey.getFileName().toString().toLowerCase(Locale.ROOT));
					Repo repo = new Repo();
					for (Path pathJre : listPath(pathKey)) {
						// Create json from all jvm
						Repo createdJson = createRepo(pathKey.getParent(), pathJre, 
							configFile.getAppName(), configFile);
						Path jvmConfig = Paths.get("jvms", type.toString().toLowerCase(Locale.ROOT), arch.toString(),
								key, pathJre.getFileName() + ".json");
						fileMapperService.write(createdJson, jvmConfig.toString());
						repo.setResources(Arrays.asList(Metadata.createMetadata(jvmConfig)));
						repo.setRepositories(configFile.getDomain());
					}
					jvm.getJvms().get(type).get(arch).put(key, repo);
				}
			}
		}
		return jvm;
	}
}