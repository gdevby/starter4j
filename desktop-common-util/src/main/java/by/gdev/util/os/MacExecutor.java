package by.gdev.util.os;

import com.sun.jna.Library;
import com.sun.jna.Native;

import by.gdev.util.model.GPUsDescriptionDTO;

import java.io.IOException;

public class MacExecutor extends LinuxExecutor {
    //todo test
    @Override
    public boolean isIdleWithoutInputEventsMoreThan(int seconds) {
        double idleTimeSeconds = ApplicationServices.INSTANCE.CGEventSourceSecondsSinceLastEventType(ApplicationServices.kCGEventSourceStateCombinedSessionState, ApplicationServices.kCGAnyInputEventType);
        return false; //(long) (idleTimeSeconds * 1000);
    }

    @Override
    public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
        String res = execute("system_profiler SPDisplaysDataType", 60);
        return getGPUInfo1(res, "chipset model:");
    }

    public interface ApplicationServices extends Library {

        ApplicationServices INSTANCE = Native.load("ApplicationServices", ApplicationServices.class);

        int kCGAnyInputEventType = ~0;
        int kCGEventSourceStatePrivate = -1;
        int kCGEventSourceStateCombinedSessionState = 0;
        int kCGEventSourceStateHIDSystemState = 1;

        /**
         * @param sourceStateId
         * @param eventType
         * @return the elapsed seconds since the last input event
         * @see http://developer.apple.com/mac/library/documentation/Carbon/Reference/QuartzEventServicesRef/Reference/reference.html#//apple_ref/c/func/CGEventSourceSecondsSinceLastEventType
         */
        double CGEventSourceSecondsSinceLastEventType(int sourceStateId, int eventType);
    }



}
