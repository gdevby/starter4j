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
	//TODO with Upper character you should start. First character of the sentence.
	/**
	 * hash sum of the file, using the SHA-1 hashing algorithm
	 */
    private String sha1;
	/**
	 * file size
	 */
    private long size;
    /**
     * the path to the file
     */
    private String path;
    //TODO ?
    private List<String> urls ;
    /**
     * relative path to the file, first check urls after relativeUrl
     */
    private String relativeUrl; 
    /**
     * If executable=true?, this file is executable/
     */
    private boolean executable;

    public static Metadata createMetadata (Path config) throws NoSuchAlgorithmException, IOException {
		Metadata metadata = new Metadata();
		metadata.setPath(config.toString());
		metadata.setRelativeUrl(config.toString());
		metadata.setSha1(DesktopUtil.getChecksum(config.toFile(), "sha-1"));
		return metadata;
    }
}
