package com.qaverse.smart.logger;

import java.util.function.Supplier;

/**
 * The only class most application/test developers need to know.
 */
public final class SmartLog {

	private SmartLog() {
	}

	public static void trace(String message) {
		SmartLogger.log(SmartLogLevel.TRACE, message, null, SmartLogEventType.LOG, null);
	}

	public static void debug(String message) {
		SmartLogger.log(SmartLogLevel.DEBUG, message, null, SmartLogEventType.LOG, null);
	}

	/**
	 * Lazy DEBUG logging.
	 *
	 * <p>
	 * The message supplier is evaluated only when DEBUG logging is enabled. This
	 * avoids unnecessary String construction on the disabled DEBUG path.
	 * </p>
	 */
	public static void debug(Supplier<String> messageSupplier) {

		if (messageSupplier == null || !SmartLogger.isEnabled(SmartLogLevel.DEBUG)) {
			return;
		}

		debug(messageSupplier.get());
	}

	public static void info(String message) {
		SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.LOG, null);
	}

	public static void warn(String message) {
		SmartLogger.log(SmartLogLevel.WARN, message, null, SmartLogEventType.LOG, null);
	}

	public static void error(String message) {
		SmartLogger.log(SmartLogLevel.ERROR, message, null, SmartLogEventType.LOG, null);
	}

	public static void error(String message, Throwable throwable) {
		SmartLogger.log(SmartLogLevel.ERROR, message, throwable, SmartLogEventType.LOG, null);
	}

	public static void step(String message) {
		SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.STEP, null);
	}

	public static void success(String message) {
		SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.SUCCESS, null);
	}

	public static void failure(String message, Throwable throwable) {
		SmartLogger.log(SmartLogLevel.ERROR, message, throwable, SmartLogEventType.FAILURE, null);
	}

	public static SmartTimer startTimer(String name) {
		return new SmartTimer(name);
	}

	public static void timed(String name, long durationNanos) {
		SmartLogger.timed(name, durationNanos);
	}

	public static boolean isTraceEnabled() {
		return SmartLogger.isEnabled(SmartLogLevel.TRACE);
	}

	public static boolean isDebugEnabled() {
		return SmartLogger.isEnabled(SmartLogLevel.DEBUG);
	}

	public static boolean isInfoEnabled() {
		return SmartLogger.isEnabled(SmartLogLevel.INFO);
	}

	public static void trace(Supplier<String> messageSupplier) {
		if (messageSupplier == null || !SmartLogger.isEnabled(SmartLogLevel.TRACE)) {
			return;
		}
		trace(messageSupplier.get());
	}

	public static void info(Supplier<String> messageSupplier) {
		if (messageSupplier == null || !SmartLogger.isEnabled(SmartLogLevel.INFO)) {
			return;
		}
		info(messageSupplier.get());
	}

	public static void warn(Supplier<String> messageSupplier) {
		if (messageSupplier == null || !SmartLogger.isEnabled(SmartLogLevel.WARN)) {
			return;
		}
		warn(messageSupplier.get());
	}

	public static void error(Supplier<String> messageSupplier) {
		if (messageSupplier == null || !SmartLogger.isEnabled(SmartLogLevel.ERROR)) {
			return;
		}
		error(messageSupplier.get());
	}
}