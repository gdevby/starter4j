package by.gdev.util.os.additional;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class MacExecutorAdditional extends LinuxExecutorAdditional implements OSExecutorAdditional {
	//Not correct work
	@Override
	public boolean isIdleWithoutInputEventsMoreThan(int seconds) {
		double idleTimeSeconds = ApplicationServices.INSTANCE.CGEventSourceSecondsSinceLastEventType(
				ApplicationServices.kCGEventSourceStateCombinedSessionState, ApplicationServices.kCGAnyInputEventType);
		return false; // (long) (idleTimeSeconds * 1000);
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
