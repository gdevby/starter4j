package desktop.starter.component.config;

public class File {
    private String sha1;
    private long size;
    private String path;
    private String url;

    public File(String sha1, long size, String path, String url) {
        this.sha1 = sha1;
        this.size = size;
        this.path = path;
        this.url = url;
    }

    public String getSha1() {
        return sha1;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }
}
