package com.qaverse.smart.logger;

import com.qaverse.smart.logger.internal.SmartLoggerEngine;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Global runtime controller for Smart Logger.
 *
 * Application code should normally use SmartLog. This class controls destinations,
 * level and integrations.
 */
public final class SmartLogger {
    private static final AtomicReference<SmartLoggerConfig> CONFIG =
            new AtomicReference<>(SmartLoggerConfig.fromSystemProperties());
    private static final AtomicReference<SmartReportSink> REPORT_SINK = new AtomicReference<>();

    private SmartLogger() { }

    public static SmartLoggerConfig configure() {
        return CONFIG.get();
    }

    public static void reload() {
        CONFIG.set(SmartLoggerConfig.fromSystemProperties());
    }

    public static void setReportSink(SmartReportSink sink) {
        REPORT_SINK.set(sink);
    }

    public static void clearReportSink() {
        REPORT_SINK.set(null);
    }

    public static void enableConsole() { CONFIG.get().setConsoleEnabled(true); }
    public static void disableConsole() { CONFIG.get().setConsoleEnabled(false); }
    public static void enableFile() { CONFIG.get().setFileEnabled(true); }
    public static void disableFile() { CONFIG.get().setFileEnabled(false); }
    public static void enableReport() { CONFIG.get().setReportEnabled(true); }
    public static void disableReport() { CONFIG.get().setReportEnabled(false); }

    public static boolean isConsoleEnabled() { return CONFIG.get().isConsoleEnabled(); }
    public static boolean isFileEnabled() { return CONFIG.get().isFileEnabled(); }
    public static boolean isReportEnabled() { return CONFIG.get().isReportEnabled(); }

    public static void setLevel(SmartLogLevel level) {
        CONFIG.get().setLevel(level);
    }

    public static SmartLogLevel getLevel() {
        return CONFIG.get().getLevel();
    }

    static boolean isEnabled(SmartLogLevel level) {
        return SmartLoggerEngine.isEnabled(CONFIG.get(), level);
    }

    static void log(SmartLogLevel level, String message, Throwable throwable,
                    SmartLogEventType eventType, Long durationNanos) {
        SmartLoggerEngine.log(CONFIG.get(), REPORT_SINK.get(), level, message, throwable, eventType, durationNanos);
    }

    static void timed(String name, long durationNanos) {
        log(SmartLogLevel.DEBUG,
                name + " completed in " + durationNanos / 1_000_000.0 + " ms",
                null,
                SmartLogEventType.TIMING,
                durationNanos);
    }
}
