package by.gdev.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
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

/**
 * This class is designed to convert files required for configuration into json
 * format. At the output, we receive a json files with a description of the
 * metadata of each file in the input directory
 */
public class AppConfigCreator {
	public static final String APP_CONFIG_GENERATOR = "appConfigModel.json";
	public static final String DOMAIN_CONFIG = "domainConfig.json";
	public static final String APP_CONFIG = "appConfig.json";
	public static final String TARGET_OUT_FOLDER = "target/out";
	public static final String APP_DEPENDENCISES_CONFIG = "dependencies.json";
	public static final String APP_RESOURCES_CONFIG = "resources.json";
	public static final String JAVA_CONFIG = "javaConfig.json";

	FileMapperService fileMapperService;

	public AppConfigCreator(FileMapperService fileMapperService) {
		this.fileMapperService = fileMapperService;
	}

	public AppConfig createConfig(AppConfigModel configFile) throws IOException, NoSuchAlgorithmException {
		AppConfig appConfig = new AppConfig();
		String version = Paths.get(configFile.getAppName(), String.valueOf(configFile.getAppVersion())).toString();
		Path appFolder = Paths.get(configFile.getAppFolder());
		Path dependencies = Paths.get(configFile.getAppDependencies());
		Path resources = Paths.get(configFile.getAppResources());
		Path dependenciesConfig = Paths.get(TARGET_OUT_FOLDER, version, APP_DEPENDENCISES_CONFIG);
		Path resourcesConfig = Paths.get(TARGET_OUT_FOLDER, version, APP_RESOURCES_CONFIG);
		FileUtils.copyDirectory(resources.toFile(), Paths.get(TARGET_OUT_FOLDER, version).toFile());
		FileUtils.copyDirectory(dependencies.toFile(), Paths.get(TARGET_OUT_FOLDER, version, "dependencies").toFile());
		FileUtils.copyFile(Paths.get(appFolder.toString(), configFile.getAppJar().toString()).toFile(),
				Paths.get(TARGET_OUT_FOLDER, version, configFile.getAppJar().toString()).toFile());
		fileMapperService.write(createJreConfig(configFile),
				Paths.get(TARGET_OUT_FOLDER, configFile.getAppName(), JAVA_CONFIG).toString());
		fileMapperService.write(createRepo(dependencies.getParent(), dependencies,
				String.valueOf(configFile.getAppVersion()), configFile), dependenciesConfig.toString());
		fileMapperService.write(
				createRepo(resources, resources, String.valueOf(configFile.getAppVersion()), configFile),
				resourcesConfig.toString());
		appConfig.setAppName(configFile.getAppName());
		appConfig.setAppVersion(configFile.getAppVersion());
		appConfig.setAppArguments(configFile.getAppArguments());
		appConfig.setJvmArguments(configFile.getJvmArguments());
		appConfig.setMainClass(configFile.getMainClass());
		appConfig.setAppFileRepo(createRepo(appFolder, Paths.get(configFile.getAppFolder(), configFile.getAppJar()),
				String.valueOf(configFile.getAppVersion()), configFile));
		appConfig.setAppDependencies(createRepo(Paths.get(TARGET_OUT_FOLDER, version), dependenciesConfig,
				String.valueOf(configFile.getAppVersion()), configFile));
		appConfig.setAppResources(createRepo(Paths.get(TARGET_OUT_FOLDER, version), resourcesConfig,
				String.valueOf(configFile.getAppVersion()), configFile));
		if (!configFile.isSkinJVMGeneration()) {
			createJreConfig(configFile);
			appConfig.setJavaRepo(createRepo(Paths.get(TARGET_OUT_FOLDER, configFile.getAppName()),
					Paths.get(TARGET_OUT_FOLDER, configFile.getAppName(), JAVA_CONFIG), null, configFile));
		} else {
			AppConfig app = fileMapperService.read(Paths.get(configFile.getJavaConfig(), APP_CONFIG).toString(),
					AppConfig.class);
			appConfig.setJavaRepo(app.getJavaRepo());
		}
		return appConfig;
	}

	private Repo createRepo(Path jvms, Path folder, String str, AppConfigModel configFile) throws IOException {
		List<Metadata> metadataList = Files.walk(folder).filter(Files::isRegularFile).map(DesktopUtil.wrap(e -> {
			Path s = jvms.relativize(e);
			String simvLink = "";
			Metadata m = new Metadata();
			if (s.endsWith("java") | s.endsWith("java.exe"))
				m.setExecutable(true);
			m.setSha1(DesktopUtil.getChecksum(e.toFile(), "SHA-1"));
			m.setPath(s.toString().replace("\\", "/"));
			m.setSize(e.toFile().length());
			BasicFileAttributes attr = Files.readAttributes(e, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			if (attr.isSymbolicLink()) {
				File link = new File(Files.readSymbolicLink(e).toString());
				if (link.getParent() == null) {
					simvLink = Paths.get(s.getParent().toString(), link.toString()).toString();
					m.setSha1("");
					m.setSize(0);
				} else {
					simvLink = Paths.get(s.getParent().getParent().toString(), link.getName()).toString();
					m.setSha1("");
					m.setSize(0);
				}
			}
			m.setLink(simvLink.toString());
			if (Objects.nonNull(str))
				s = Paths.get(str, s.toString());
			m.setRelativeUrl(s.toString().replace("\\", "/"));
			return m;
		})).collect(Collectors.toList());
		Repo r = new Repo();
		r.setResources(metadataList);
		r.setRepositories(createUrl(configFile));
		return r;
	}

	private List<Path> listPath(Path p) throws IOException {
		return Files.walk(p, 1).filter(entry -> !entry.equals(p)).collect(Collectors.toList());
	}

	JVMConfig createJreConfig(AppConfigModel configFile) throws IOException, NoSuchAlgorithmException {
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
					if (!(key.endsWith(".tar.gz") || key.endsWith(".zip")))
						throw new RuntimeException("inaccessible jre archive format, use .tar.gz or .zip");
					String javaFolder = Paths.get(configFile.getJavaFolder()).getFileName().toString();
					String str = Paths.get(javaFolder, type.toString().toLowerCase(), arch.toString()).toString();
					Repo repo = createRepo(pathKey.getParent(), pathKey, str, configFile);
					jvm.getJvms().get(type).get(arch).put("jre_default", repo);
				}
			}
		}
		return jvm;
	}

	private List<String> createUrl(AppConfigModel configFile) {
		return configFile.getUrl().stream().map(e -> String.format("%s%s/", e, configFile.getAppName()))
				.collect(Collectors.toList());
	}
}