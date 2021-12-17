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

import org.apache.commons.lang3.ArrayUtils;

import com.beust.jcommander.Parameter;

import by.gdev.util.DesktopUtil;
import by.gdev.util.OSInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarterAppConfig{
	private static final String APP_CONFIG = "/appConfig.json";
	
	//Size in megabytes
	@Parameter(names = "-memory", description = "The size of the required free disk space to download the application")
    private long minMemorySize;
	
	@Parameter(names = "-mainAppConfig", description = "URI of the directory in which appConfig.json is located, which contains all information about the application being launched, this config is used by all applications by default")
	private String serverFile;
	
	@Parameter(names = "-workDirectory", description = "Working directory where the files required for the application will be loaded and in which the application will be launched. The param used for test. "
			+ "The second way is to put in file with installer.  The file name is installer.properties which contains work.dir=... This is for production. "
			+ "The default method is DesktopUtil.getSystemPath defined with by.gdev.Main. The priority: StarterAppConfig.workDirectory, file installer.properties and default method")
	private String workDirectory;
	
	@Parameter(names = "-version", description = "Specifies the version of the application to launch. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. "
			+ "This way we can install old versions of the application. For this you need set exactly version.")
	private Double version;
	
	@Parameter(names = "-urlConnection", description = "List of sites for checking Internet connection access")
	private List <String> urlConnection;
	
	public static final StarterAppConfig DEFAULT_CONFIG;
	static {
		DEFAULT_CONFIG = new StarterAppConfig(500, 
//				"http://localhost:81/starter-app/1.0", //old version
				"https://raw.githubusercontent.com/gdevby/starter-app/master/example-compiled-app/server/starter-app/1.0", //download from github
				"target/test_folder/testContainer/",
				null, Arrays.asList("http://www.google.com","http://www.baidu.com"));
	}
	
	
	public String getServerFileConifg(StarterAppConfig config) {
		if (Objects.isNull(config.getVersion()))
			return config.getServerFile() + APP_CONFIG;
		else {
			String[] str = config.getServerFile().split("/");
			String[] newstr = ArrayUtils.insert(str.length, str, String.valueOf(config.getVersion()));
			String joinedString = String.join("/", newstr) + APP_CONFIG;
			return joinedString;
		}
	}
	
	public String workDir(String workDirectory) throws IOException {
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
		 else return DesktopUtil.getSystemPath(OSInfo.OSType.LINUX, "starter").toString();
	}
}