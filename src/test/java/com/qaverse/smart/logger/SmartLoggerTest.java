package com.qaverse.smart.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SmartLoggerTest {

	 static {
	        System.setProperty(
	                "smart.logger.file.dir",
	                "target/test-logs"
	        );
	    }

	 
    private static final Path LOG_FILE = Path.of("target/test-logs/Automation.log");

    @AfterEach
    void cleanup() {
        SmartLogger.disableConsole();
        SmartLogger.disableFile();
        SmartLogger.disableReport();
        SmartLogger.clearReportSink();
        SmartLogger.setLevel(SmartLogLevel.INFO);
    }

    @Test
    void basicLoggingAndSemanticEvents() {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableReport();

        SmartLog.info("INFO message");
        SmartLog.warn("WARNING message");
        SmartLog.error("ERROR message");
        SmartLog.step("Login Step");
        SmartLog.success("Login successful");
        SmartLog.failure("Login failed", new RuntimeException("Element not found"));

        assertEquals(6, events.size());
        assertEquals(SmartLogEventType.LOG, events.get(0).getEventType());
        assertEquals(SmartLogEventType.STEP, events.get(3).getEventType());
        assertEquals(SmartLogEventType.SUCCESS, events.get(4).getEventType());
        assertEquals(SmartLogEventType.FAILURE, events.get(5).getEventType());
        assertNotNull(events.get(5).getThrowable());
    }

    @Test
    void consoleToggle() {
        SmartLogger.enableConsole();
        SmartLog.info("Console ON");

        SmartLogger.disableConsole();
        SmartLog.info("Console OFF");
    }

    @Test
    void fileToggle() throws Exception {
        Files.createDirectories(LOG_FILE.getParent());

        // Do not delete an active Log4j2 file appender's file between tests.
        // On Unix, deleting an open file unlinks the directory entry while the
        // appender can continue writing to the old inode. Truncate instead.
        if (Files.exists(LOG_FILE)) {
            Files.writeString(LOG_FILE, "");
        }

        SmartLogger.enableFile();
        SmartLog.info("File ON");

        // Log4j2 RollingFile writes synchronously with this configuration.
        assertTrue(Files.exists(LOG_FILE), "File logging should create the log file");
        String content = Files.readString(LOG_FILE);
        assertTrue(content.contains("File ON"));

        long sizeAfterOn = Files.size(LOG_FILE);

        SmartLogger.disableFile();
        SmartLog.info("File OFF");

        assertEquals(sizeAfterOn, Files.size(LOG_FILE), "Disabled file logging must not append");
    }

    @Test
    void reportToggle() {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);

        SmartLogger.enableReport();
        SmartLog.step("Report ON");
        assertEquals(1, events.size());

        SmartLogger.disableReport();
        SmartLog.step("Report OFF");
        assertEquals(1, events.size());
    }

    @Test
    void multipleDestinationsUseSingleReportEvent() {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableConsole();
        SmartLogger.enableFile();
        SmartLogger.enableReport();

        SmartLog.info("ALL DESTINATIONS ON");

        assertEquals(1, events.size(), "One SmartLog call must create one report event");
        assertEquals("ALL DESTINATIONS ON", events.get(0).getMessage());
    }

    @Test
    void debugIsFilteredAtInfoLevel() {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableReport();
        SmartLogger.setLevel(SmartLogLevel.INFO);

        SmartLog.debug("hidden");
        SmartLog.info("visible");

        assertEquals(1, events.size());
        assertEquals("visible", events.get(0).getMessage());
    }

    @Test
    void debugIsVisibleWhenDebugEnabled() {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableReport();
        SmartLogger.setLevel(SmartLogLevel.DEBUG);

        SmartLog.debug("visible debug");

        assertEquals(1, events.size());
        assertEquals(SmartLogLevel.DEBUG, events.get(0).getLevel());
    }

    @Test
    void timerProducesTimingEventWhenDebugEnabled() throws InterruptedException {
        List<SmartLogEvent> events = new ArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableReport();
        SmartLogger.setLevel(SmartLogLevel.DEBUG);

        try (SmartTimer ignored = SmartLog.startTimer("Sample operation")) {
            Thread.sleep(20);
        }

        assertEquals(1, events.size());
        SmartLogEvent event = events.get(0);
        assertEquals(SmartLogEventType.TIMING, event.getEventType());
        assertNotNull(event.getDurationNanos());
        assertTrue(event.getDurationNanos() >= 0);
    }
    @Test
    void reportSinkFailureMustNotBreakAutomation() {
        SmartLogger.setReportSink(event -> {
            throw new RuntimeException("synthetic report failure");
        });
        SmartLogger.enableReport();

        assertDoesNotThrow(() -> SmartLog.info("execution must continue"));
    }

    @Test
    void concurrentReportLoggingIsSafe() throws Exception {
        List<SmartLogEvent> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        SmartLogger.setReportSink(events::add);
        SmartLogger.enableReport();

        int threads = 8;
        int perThread = 1_000;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        SmartLog.info("concurrent");
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
            thread.start();
        }

        start.countDown();
        done.await();

        assertEquals(0, failures.get());
        assertEquals(threads * perThread, events.size());
    }

}
