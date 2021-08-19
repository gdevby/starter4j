package by.gdev.model;

import com.beust.jcommander.Parameter;

import lombok.Data;

@Data
public class StarterAppConfig{
	
	//Size in bytes
	@Parameter(names = "-memory", description = "")
    private long minMemorySize = 524288000;
}
