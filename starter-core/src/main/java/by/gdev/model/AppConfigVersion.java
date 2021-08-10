package by.gdev.model;

import lombok.Data;

import java.util.Map;
/**
 *The main config of the starter to manage updating
 */
@Data
public class AppConfigVersion {
    /**
     * Used new version of the app, users can't skip.
     * if true uses first version from AppConfigVersion#versions
     * if false starter shows to an user the choose: update or not(used all config without replacing)
     */
    private boolean mandatory;
    /**
     *
     */
    private Map<String,RepoVersion> versions;
}
