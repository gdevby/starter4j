package by.gdev.util.model.download;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import by.gdev.util.DesktopUtil;

@Data
//TODO  add for every class descriptoin (ready)
/**
 * This class is intended to describe information about the file that will be used in the configuration. The file information includes the following data:
 * sha1 - hash sum of the file, using the SHA-1 hashing algorithm.
 * size - file size.
 * path - the path to the file.
 * relativeUrl - relative path to the file.
 *
 */
public class Metadata {
    private String sha1;
    private long size;
    private String path;
    private List<String> urls ;
    /**
     * Related url, first check urls after relativeUrl
     */
    private String relativeUrl; 
    private boolean executable;

    public static Metadata createMetadata (Path config) throws NoSuchAlgorithmException, IOException {
		Metadata metadata = new Metadata();
		metadata.setPath(config.toString());
		metadata.setRelativeUrl(config.toString());
		metadata.setSha1(DesktopUtil.getChecksum(config.toFile(), "sha-1"));
		return metadata;
    }
}
