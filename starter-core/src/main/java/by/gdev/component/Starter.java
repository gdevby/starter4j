package by.gdev.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I want to see all possible implementations and idea.
 * So we can implement upper abstraction with system.out messages!
 */
public class Starter {

    private Logger log;

    public Starter() {
        log = LoggerFactory.getLogger(getClass());
    }

    /**
     * Get information about current OS
     */
    public void collectOSInfo() {


        log.info("call method {}", "collectOSInfo");
    }

    public void checkCommonProblems() {
        log.info("call method {}", "checkCommonProblems");
    }

    /**
     * Validate files,java  and return what we need to download
     */
    public Object validate() {
        log.info("call method {}", "validate");
        return null;
    }

    /**
     * Download resources(java,files,.jar) and and prepare them to use,
     * after this we need revalidate again
     *
     * @return
     */
    public Object prepareResources() {
        log.info("call method {}", "prepareResources");
        return null;
    }

    /**
     * Run app and wait some command to switch off , cas we run in new process
     * switch off command 'Starter run app'
     *
     * @return
     */
    public Object runApp() {
        log.info("call method {}", "runApp");
        return null;
    }

}
