package desktop.starter.model;

import lombok.Data;

import java.util.List;

@Data
public class Metadata {
    private String sha1;
    private long size;
    private String path;
    private List<String> urls ;
    /**
     * Related url, first check url after relUrl
     */
    private String relUrl;

}
