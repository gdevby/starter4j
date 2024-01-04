package by.gdev.generator.model;

import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class contains parameters for loading files needed to create config files
 * 
 * @author Robert Makrytski
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigModel {
	@Parameter(names = "-name", description = "Application Name")
	private String appName;
	@Parameter(names = "-version", description = "Application version, example 1.2.4 or 0.9.0.1")
	private String appVersion;
	@Parameter(names = "-mainClass", description = "The main class for running the application")
	private String mainClass;
	@Parameter(names = "-appArguments", description = "Application arguments")
	private List<String> appArguments;
	@Parameter(names = "-jvmArguments", description = "Arguments for jvm")
	private List<String> jvmArguments;
	@Parameter(names = "-appJar", description = "Jar app file to run the application")
	private String appJar;
	@Parameter(names = "-javaFolder", description = "Input directory where jvm are stored to create configuration for java, lets you skip java generation if the argument is -skinJVMGeneration=true")
	private String javaFolder;
	@Parameter(names = "-javaConfig", description = "Directory where saved the result of the jvm configuration. We can create once the config and using all times.")
	private String javaConfig;
	@Parameter(names = "-resources", description = "Directory with the necessary resources to run the application")
	private String appResources;
	@Parameter(names = "-dependencies", description = "Directory with the necessary dependencies to run the application")
	private String appDependencies;
	@Parameter(names = "-appFolder", description = "Directory with your desktop app. It tries to find in target/appName-version.jar")
	private String appFolder;
	@Parameter(names = "-url", description = "used url(https://example.com) to generate configurations for all resources to download from this in future")
	private List<String> url;
	@Parameter(names = "-ignoreResourcesFolders", description = "Directories that should be excluded when creating the configuration from resources")
	private List<String> ignoreFolders;
	@Parameter(names = "-skipJVMGeneration", description = "Flag to skip java generation. Skipping java configuration will speed up the creation of application configs, because you do it once", arity = 1)
	private boolean skipJVMGeneration = false;
	@Parameter(names = "-help", help = true)
	public boolean help = false;

	public static final AppConfigModel DEFAULT_APP_CONFIG_MODEL = new AppConfigModel("starter-app", "1.0",
			"desktop.starter.app.Main", Arrays.asList("currentAppVersion={currentAppVersion}"),
			Arrays.asList("-Xmx512m", "-Dfile.encoding=UTF8", "-Djava.net.preferIPv4Stack=true"), "starter-app-1.0.jar",
			"../../starter-app/example-compiled-app/jres_default", "src/test/resources",
			"../../starter-app/src/main/resources", "../../starter-app/example-compiled-app/target/dependencies",
			"../../starter-app/example-compiled-app/target",
			Arrays.asList("https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/"),
			Arrays.asList(), false, false);
}