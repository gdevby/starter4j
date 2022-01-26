package by.gdev.model;

import lombok.Data;
/**
 * Saved on client parameters for the for starter  
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class AppLocalConfig {
	private String currentAppVersion;
	private String skipUpdateVersion;
	public boolean isSkippedVersion(String newVersion) {
		return newVersion.equals(skipUpdateVersion);
	}

}
