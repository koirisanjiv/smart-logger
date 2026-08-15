package com.qaverse.smart.logger;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.qaverse.smart.logger.internal.SmartLoggerEngine;

/**
 * Global runtime controller for Smart Logger.
 *
 * Application code should normally use SmartLog.
 * This class controls destinations, level and integrations.
 */
public final class SmartLogger {

    private static final AtomicReference<SmartLoggerConfig> CONFIG =
            new AtomicReference<>(SmartLoggerConfig.fromSystemProperties());

    private static final AtomicReference<SmartReportSink> REPORT_SINK =
            new AtomicReference<>();

    private static volatile boolean log4jInitialized = false;

    static {
        initializeLog4j();
    }
    
    private SmartLogger() {
    }

    public static SmartLoggerConfig configure() {
        return CONFIG.get();
    }

    /**
     * Reload Smart Logger configuration and Log4j2 configuration.
     *
     * This is important when smart.logger.file.dir or other logger
     * system properties are changed at runtime.
     */
    public static synchronized void reload() {

        SmartLoggerConfig config =
                SmartLoggerConfig.fromSystemProperties();

        CONFIG.set(config);

        initializeLog4j();
    }

    /**
     * Initialize Log4j2 using the configuration packaged inside
     * the Smart Logger JAR.
     */
    private static synchronized void initializeLog4j() {

        try {

            URL configUrl = SmartLogger.class
                    .getClassLoader()
                    .getResource("log4j2-smart-logger.xml");

            if (configUrl == null) {

                throw new IllegalStateException(
                        "Smart Logger Log4j2 configuration not found: "
                                + "log4j2-smart-logger.xml"
                );
            }

            URI configUri = configUrl.toURI();

            LoggerContext context =
                    (LoggerContext) LogManager.getContext(false);

            context.setConfigLocation(configUri);
            context.reconfigure();

            log4jInitialized = true;

            System.out.println(
                    "[SMART-LOGGER] Log4j2 configuration loaded: "
                            + configUrl
            );

        } catch (Exception e) {

            log4jInitialized = false;

            System.err.println(
                    "[SMART-LOGGER] Failed to initialize Log4j2: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    public static boolean isLog4jInitialized() {
        return log4jInitialized;
    }

    public static void setReportSink(SmartReportSink sink) {
        REPORT_SINK.set(sink);
    }

    public static void clearReportSink() {
        REPORT_SINK.set(null);
    }

    public static void enableConsole() {
        CONFIG.get().setConsoleEnabled(true);
    }

    public static void disableConsole() {
        CONFIG.get().setConsoleEnabled(false);
    }

    public static void enableFile() {
        CONFIG.get().setFileEnabled(true);
    }

    public static void disableFile() {
        CONFIG.get().setFileEnabled(false);
    }

    public static void enableReport() {
        CONFIG.get().setReportEnabled(true);
    }

    public static void disableReport() {
        CONFIG.get().setReportEnabled(false);
    }

    public static boolean isConsoleEnabled() {
        return CONFIG.get().isConsoleEnabled();
    }

    public static boolean isFileEnabled() {
        return CONFIG.get().isFileEnabled();
    }

    public static boolean isReportEnabled() {
        return CONFIG.get().isReportEnabled();
    }

    public static void setLevel(SmartLogLevel level) {
        CONFIG.get().setLevel(level);
    }

    public static SmartLogLevel getLevel() {
        return CONFIG.get().getLevel();
    }

    static boolean isEnabled(SmartLogLevel level) {
        return SmartLoggerEngine.isEnabled(CONFIG.get(), level);
    }

    static void log(
            SmartLogLevel level,
            String message,
            Throwable throwable,
            SmartLogEventType eventType,
            Long durationNanos) {

        SmartLoggerEngine.log(
                CONFIG.get(),
                REPORT_SINK.get(),
                level,
                message,
                throwable,
                eventType,
                durationNanos
        );
    }

    static void timed(String name, long durationNanos) {

        log(
                SmartLogLevel.DEBUG,
                name + " completed in "
                        + durationNanos / 1_000_000.0
                        + " ms",
                null,
                SmartLogEventType.TIMING,
                durationNanos
        );
    }
}