package desktop.starter.model;

import lombok.Data;

import java.util.List;

/**
 * We can config direct link and part path.
 *
 */
@Data
public class Repo {
    /**
     * Example : https://github.com , ...
     */
    private List<String> repo;
    private List<Metadata> resources;
}
