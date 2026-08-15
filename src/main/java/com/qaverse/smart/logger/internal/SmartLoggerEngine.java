package com.qaverse.smart.logger.internal;

import com.qaverse.smart.logger.SmartLogEvent;
import com.qaverse.smart.logger.SmartLogEventType;
import com.qaverse.smart.logger.SmartLogLevel;
import com.qaverse.smart.logger.SmartLoggerConfig;
import com.qaverse.smart.logger.SmartReportSink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Internal execution engine. Not part of the public Smart Logger API. */
public final class SmartLoggerEngine {
    private static final Logger CONSOLE_LOGGER = LogManager.getLogger("smart.console");
    private static final Logger FILE_LOGGER = LogManager.getLogger("smart.file");

    private SmartLoggerEngine() { }

    public static boolean isEnabled(SmartLoggerConfig config, SmartLogLevel level) {
        return config.getLevel().allows(level);
    }

    public static void log(
            SmartLoggerConfig config,
            SmartReportSink reportSink,
            SmartLogLevel level,
            String message,
            Throwable throwable,
            SmartLogEventType eventType,
            Long durationNanos) {

        // Critical fast path: no level, no event object, no destination work.
        if (!config.getLevel().allows(level)) {
            return;
        }

        final boolean reportEnabled = config.isReportEnabled() && reportSink != null;

        if (!config.isConsoleEnabled() && !config.isFileEnabled() && !reportEnabled) {
            return;
        }

        if (config.isConsoleEnabled()) {
            write(CONSOLE_LOGGER, level, message, throwable);
        }

        if (config.isFileEnabled()) {
            write(FILE_LOGGER, level, message, throwable);
        }

        if (reportEnabled) {
            SmartLogEvent event = new SmartLogEvent(
                    level,
                    message,
                    "smart",
                    Thread.currentThread().getName(),
                    eventType,
                    durationNanos,
                    throwable,
                    null);

            try {
                reportSink.accept(event);
            } catch (RuntimeException sinkFailure) {
                // A report integration is never allowed to break the automation run.
                if (config.isFileEnabled()) {
                    FILE_LOGGER.debug("Smart report sink failed", sinkFailure);
                }
            }
        }
    }

    private static void write(Logger logger, SmartLogLevel level, String message, Throwable throwable) {
        switch (level) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
            default:
                throw new IllegalStateException("Unsupported log level: " + level);
        }
    }
}
