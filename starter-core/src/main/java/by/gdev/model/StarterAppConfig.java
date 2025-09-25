package by.gdev.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;

import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo.OSType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class contains parameters for loading the application, which can be
 * changed using arguments.
 * 
 * @author Robert Makrytski
 *
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarterAppConfig {

	private static final String APP_CONFIG = "appConfig.json";
	public static final String APP_CHANGES_LOG = "changes.log";
	public static final String APP_STARTER_LOCAL_CONFIG = "starter.json";
	public static final String APP_STARTER_DOMAIN_AVAILABILITY = "domainAvailabilityV1.json";
	public static final String APP_STARTER_UPDATE_CONFIG = "starterUpdate.json";

	public static final List<String> URI_APP_CONFIG = Lists.newArrayList(
			"https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/starter-app/");
	private final boolean prod = false;
	@Parameter(names = "-appName", description = "The application name, use to create directory inside home, should be in lower case")
	private String appName;
	@Parameter(names = "-memory", description = "The size of the required free disk space to download the application")
	private long minMemorySize;
	@Parameter(names = "-uriAppConfig", description = "URI of the directory in which appConfig.json is located, which contains all information about the application being launched, this config is used by all applications by default. URI must be specified without version, see version parameter description, should be end with /")
	private List<String> serverFile;
	@Parameter(names = "-workDirectory", description = "Working directory where the files required for the application will be loaded and in which the application will be launched. The param used for test. "
			+ "The second way is to put in file with installer.  The file name is installer.properties which contains work.dir=... This is for production. "
			+ "The default method is DesktopUtil.getSystemPath defined with by.gdev.Main. The priority: StarterAppConfig.workDirectory, file installer.properties and default method")
	private String workDirectory;
	@Parameter(names = "-version", description = "Specifies the version of the application to launch. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. "
			+ "This way we can install old versions of the application. For this you need set exactly version.")
	private String version;
	@Parameter(names = "-testURLs", description = "List of url which use to do requests. When some url or servers are not available, it doesn't do request. It will skip for download file and to do reuests. If we have server file http://example.com/repo than this field should be http://example.com")
	private List<String> testURLs;
	@Parameter(names = "-attempts", description = "The number of allowed attempts to restore the connection")
	private int maxAttempts;
	@Parameter(names = "-connectTimeout", description = "Set connect timeout")
	private int connectTimeout;
	@Parameter(names = "-socketTimeout", description = "Set socket timeout")
	private int socketTimeout;
	@Parameter(names = "-timeToLife", description = "The time that the file is up-to-date")
	private int timeToLife;
	@Parameter(names = "-stop", description = "Argument to stop the application")
	private boolean stop;
	@Parameter(names = "-logURIService", description = "Log service which can save logs and return code. User can send code for support. "
			+ "Doesn't implement a backend. To activate we need to use parameter ExceptionMessage#logButton=true, See ViewSubscriber#doRequest")
	private List<String> logURIService;

	public static final StarterAppConfig DEFAULT_CONFIG = new StarterAppConfig("starter", 500, URI_APP_CONFIG, null,
			null,
			Arrays.asList("http://www.google.com", "http://www.baidu.com",
					"https://github.com/gdevby/starter-app/blob/master/example-compiled-app/server/starter-app/appConfig.json"),
			3, 5000, 10000, 600000, false, null);

	public String getServerFileConfig(StarterAppConfig config, String version) {
		return Objects.isNull(version) ? String.join("/", APP_CONFIG) : String.join("/", version, APP_CONFIG);
	}

	/**
	 * This method builds the working directory.
	 */

	public void buildAbsoluteWorkDirectory(OSType osType) throws IOException {
		Path installer = Paths.get("installer.properties").toAbsolutePath();
		String dir = "";
		if (Files.exists(installer)) {
			Properties property = new Properties();
			FileInputStream fis = new FileInputStream(String.valueOf(installer));
			property.load(fis);
			dir = property.getProperty("work.dir");
		}
		if (!StringUtils.isEmpty(workDirectory)) {
			workDirectory = Paths.get(workDirectory).toAbsolutePath().toString().concat("/");
		} else if (!StringUtils.isEmpty(dir)) {
			workDirectory = Paths.get(dir).toAbsolutePath().toString().concat("/");
		} else {
			String systemDir = "." + appName;
			if (osType.equals(OSType.MACOSX)) {
				systemDir = appName;
			}
			workDirectory = DesktopUtil.getSystemPath(osType, systemDir + "/starter").getAbsolutePath().toString()
					.concat("/");
		}
	}
}