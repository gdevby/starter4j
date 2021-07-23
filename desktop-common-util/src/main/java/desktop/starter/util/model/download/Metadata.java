package desktop.starter.util.model.download;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import desktop.starter.util.DesktopUtil;

@Data
public class Metadata {
    private String sha1;
    private long size;
    private String path;
    private List<String> urls ;
    /**
     * Related url, first check urls after relativeUrl
     */
    private String relativeUrl; 
    //создать метод
    public static Metadata getResource (Path config) throws NoSuchAlgorithmException, IOException {
		Metadata metadata = new Metadata();
		metadata.setPath(config.toString());
		metadata.setRelativeUrl(config.toString());
		metadata.setSha1(DesktopUtil.getChecksum(config.toFile(), "sha-1"));
		return metadata;
    }
}
