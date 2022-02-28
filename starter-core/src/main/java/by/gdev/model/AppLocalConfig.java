package by.gdev.model;

import lombok.Data;

/**
 * Saved on client parameters for the starter
 * 
 * @author Robert Makrytski
 *
 */
@Data
public class AppLocalConfig {

	private String currentAppVersion;
	private String dir;
	private String skipUpdateVersion;

	public boolean isSkippedVersion(String newVersion) {
		return newVersion.equals(skipUpdateVersion);
	}

}
