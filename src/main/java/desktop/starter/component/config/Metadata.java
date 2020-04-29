package desktop.starter.component.config;

import lombok.Data;

@Data
public class Metadata {
    private String sha1;
    private long size;
    private String path;
    private String url;
    /**
     * Related url, first check url after relUrl
     */
    private String relUrl;

}
