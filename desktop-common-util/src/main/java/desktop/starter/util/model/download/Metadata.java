package desktop.starter.util.model.download;

import lombok.Data;

import java.util.List;

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

}
