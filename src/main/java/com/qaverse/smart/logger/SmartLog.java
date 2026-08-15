package com.qaverse.smart.logger;

/**
 * The only class most application/test developers need to know.
 */
public final class SmartLog {
    private SmartLog() { }

    public static void trace(String message) { SmartLogger.log(SmartLogLevel.TRACE, message, null, SmartLogEventType.LOG, null); }
    public static void debug(String message) { SmartLogger.log(SmartLogLevel.DEBUG, message, null, SmartLogEventType.LOG, null); }
    public static void info(String message) { SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.LOG, null); }
    public static void warn(String message) { SmartLogger.log(SmartLogLevel.WARN, message, null, SmartLogEventType.LOG, null); }
    public static void error(String message) { SmartLogger.log(SmartLogLevel.ERROR, message, null, SmartLogEventType.LOG, null); }
    public static void error(String message, Throwable throwable) { SmartLogger.log(SmartLogLevel.ERROR, message, throwable, SmartLogEventType.LOG, null); }

    public static void step(String message) { SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.STEP, null); }
    public static void success(String message) { SmartLogger.log(SmartLogLevel.INFO, message, null, SmartLogEventType.SUCCESS, null); }
    public static void failure(String message, Throwable throwable) { SmartLogger.log(SmartLogLevel.ERROR, message, throwable, SmartLogEventType.FAILURE, null); }

    public static SmartTimer startTimer(String name) { return new SmartTimer(name); }
    public static void timed(String name, long durationNanos) { SmartLogger.timed(name, durationNanos); }

    public static boolean isTraceEnabled() { return SmartLogger.isEnabled(SmartLogLevel.TRACE); }
    public static boolean isDebugEnabled() { return SmartLogger.isEnabled(SmartLogLevel.DEBUG); }
    public static boolean isInfoEnabled() { return SmartLogger.isEnabled(SmartLogLevel.INFO); }

}
