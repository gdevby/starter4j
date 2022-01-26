package by.gdev.model;

import lombok.Data;

import java.util.List;

/**
 * We can config direct link and part path.
 * @author Robert Makrytski
 */
@Data
public class RepoVersion {
    /**
     * Example : https://github.com
     */
    private List<String> repo;
    private List<String> urls;
    private String relUrl;

}
