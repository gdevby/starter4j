package by.gdev.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

/**
 * I want to see all possible implementations and idea.
 * So we can implement upper abstraction with system.out messages!
 */
@Slf4j
public class Bootstrapper {


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
        return null;    }

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
