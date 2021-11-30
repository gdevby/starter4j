package by.gdev.model;

import com.beust.jcommander.Parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StarterAppConfig{
	
	//Size in bytes
	@Parameter(names = "-memory", description = "")
    private long minMemorySize;
	
	@Parameter(names = "-serverFile", description = "Path to tempAppConfig.json")
	private String serverFile;
	
	@Parameter(names = "-output", description = "directory where everything is saved")
	private String container;
	
	public static final StarterAppConfig DEFAULT_CONFIG;
	
	static {
		DEFAULT_CONFIG = new StarterAppConfig(524288000, 
				"http://localhost:81/server/tempAppConfig.json",
				"/home/aleksandr/Desktop/qwert/container1/"
				);
	}
	
	
	
}
