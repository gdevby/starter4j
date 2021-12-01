package by.gdev.model;


import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.beust.jcommander.Parameter;

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
	
	@Parameter(names = "-workDirectory", description = "Working directory where the files required for the application will be loaded and in which the application will be launched")
	private String container;
	
	@Parameter(names = "-version", description = "Specifies the version of the application to launch. Therefore, the config http://localhost:81/app/1.0/appConfig.json for version 1.0 will be used. This way we can install old versions of the application.")
	private Double version;
	
	public static final StarterAppConfig DEFAULT_CONFIG;
	static {
		DEFAULT_CONFIG = new StarterAppConfig(500, 
				"http://localhost:81/app",
				"target/test_folder/testContainer/",
				null);
	}
	
	public String controlVersion(StarterAppConfig config) {
		if (Objects.isNull(config.getVersion()))
			return config.getServerFile() + APP_CONFIG;
		else {
			String[] str = config.getServerFile().split("/");
			String[] newstr = ArrayUtils.insert(str.length, str, String.valueOf(config.getVersion()));
			String joinedString = String.join("/", newstr) + APP_CONFIG;
			return joinedString;
		}
	}
}