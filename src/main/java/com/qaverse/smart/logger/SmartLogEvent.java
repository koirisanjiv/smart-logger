package com.qaverse.smart.logger;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public final class SmartLogEvent {
    private final Instant timestamp;
    private final SmartLogLevel level;
    private final String message;
    private final String module;
    private final String threadName;
    private final SmartLogEventType eventType;
    private final Long durationNanos;
    private final Throwable throwable;
    private final Map<String, Object> metadata;

    public SmartLogEvent(
            SmartLogLevel level,
            String message,
            String module,
            String threadName,
            SmartLogEventType eventType,
            Long durationNanos,
            Throwable throwable,
            Map<String, Object> metadata) {
        this.timestamp = Instant.now();
        this.level = level;
        this.message = message;
        this.module = module;
        this.threadName = threadName;
        this.eventType = eventType == null ? SmartLogEventType.LOG : eventType;
        this.durationNanos = durationNanos;
        this.throwable = throwable;
        this.metadata = metadata == null ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    public Instant getTimestamp() { return timestamp; }
    public SmartLogLevel getLevel() { return level; }
    public String getMessage() { return message; }
    public String getModule() { return module; }
    public String getThreadName() { return threadName; }
    public SmartLogEventType getEventType() { return eventType; }
    public Long getDurationNanos() { return durationNanos; }
    public Throwable getThrowable() { return throwable; }
    public Map<String, Object> getMetadata() { return metadata; }
}
