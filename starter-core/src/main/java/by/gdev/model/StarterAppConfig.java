package by.gdev.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.beust.jcommander.Parameter;

import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * The class contains parameters for loading the application, which can be changed using arguments.
 * @author Robert Makrytski
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarterAppConfig {

	private static final String APP_CONFIG = "appConfig.json";
	public static final String APP_CHANGES_LOG= "changes.log";
	public static final String APP_STARTER_LOCAL_CONFIG = "starter.json";

	@Parameter(names = "-memory", description = "The size of the required free disk space to download the application")
	private long minMemorySize;
	@Parameter(names = "-uriAppConfig", description = "URI of the directory in which appConfig.json is located, which contains all information about the application being launched, this config is used by all applications by default. URI must be specified without version, see version parameter description")
	private String serverFile;
	@Parameter(names = "-workDirectory", description = "Working directory where the files required for the application will be loaded and in which the application will be launched. The param used for test. "
			+ "The second way is to put in file with installer.  The file name is installer.properties which contains work.dir=... This is for production. "
			+ "The default method is DesktopUtil.getSystemPath defined with by.gdev.Main. The priority: StarterAppConfig.workDirectory, file installer.properties and default method")
	private String workDirectory;
	@Parameter(names = "-cacheDirectory", description = "Directory for storing caching configs")
	private Path cacheDirectory;
	@Parameter(names = "-version", description = "Specifies the version of the application to launch. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. "
			+ "This way we can install old versions of the application. For this you need set exactly version.")
	private String version;
	@Parameter(names = "-urlConnection", description = "List of sites for checking Internet connection access")
	private List<String> urlConnection;
	@Parameter(names = "-attempts", description = "The number of allowed attempts to restore the connection")
	private int maxAttempts;
	@Parameter(names = "-connectTimeout", description = "Set connect timeout")
	private int connectTimeout;
	@Parameter(names = "-socketTimeout", description = "Set socket timeout")
	private int socketTimeout;
	@Parameter(names = "-stop", description = "List of sites for checking Internet connection access")
	private boolean stop;

	public static final StarterAppConfig 
		DEFAULT_CONFIG = new StarterAppConfig(500,
				"https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/starter-app", 
				"starter/", Paths.get("starter/cache"), "1.0",
				Arrays.asList("http://www.google.com", "http://www.baidu.com"), 3, 60000, 60000, false);

	public String getServerFileConfig(StarterAppConfig config, String version) {
		if (Objects.isNull(version))
			return String.join("/", config.getServerFile(), APP_CONFIG);
		else {
			return String.join("/", config.getServerFile(), version, APP_CONFIG);
		}
	}

	/**
	 * This method returns the working directory. 
	 */
	public String workDir(String workDirectory, OSType osType) throws IOException {
		Path installer = Paths.get("installer.properties");
		String dir = "";
		if (new File(installer.toAbsolutePath().toString()).exists()) {
			Properties property = new Properties();
			FileInputStream fis = new FileInputStream(String.valueOf(installer.toAbsolutePath()));
			property.load(fis);
			dir = property.getProperty("work.dir");
		}
		if (!workDirectory.equals(""))
			return workDirectory;
		else if (!dir.equals(""))
			return dir;
		else
			return DesktopUtil.getSystemPath(osType, "starter").toString();
	}
}