package by.gdev.util.model.download;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import by.gdev.util.DesktopUtil;


/**
 * This class is intended to describe information about the file that will be used in the configuration.
 */
@Data
public class Metadata {
	/**
	 * Hash sum of the file, using the SHA-1 hashing algorithm
	 */
    private String sha1;
	/**
	 * File size
	 */
    private long size;
    /**
     * The path to the file
     */
    private String path;
    /**
     * List of URLs for which the file is available
     */
    private List<String> urls ;
    /**
     * Relative path to the file, first check urls after relativeUrl
     */
    private String relativeUrl; 
    /**
     * If executable=true, this file is executable.
     */
    private boolean executable;
    /**
     * Indicates if the file is a symbolic link
     */
    private String link;

    public static Metadata createMetadata (Path config) throws NoSuchAlgorithmException, IOException {
		Metadata metadata = new Metadata();
		metadata.setPath(config.toString());
		metadata.setRelativeUrl(config.toString());
		metadata.setSha1(DesktopUtil.getChecksum(config.toFile(), "sha-1"));
		return metadata;
    }
}
