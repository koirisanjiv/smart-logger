package com.qaverse.smart.logger;

public enum SmartLogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public boolean allows(SmartLogLevel eventLevel) {
        return eventLevel.ordinal() >= this.ordinal();
    }

    public static SmartLogLevel from(String value, SmartLogLevel fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return value.trim().toUpperCase().equals("WARNING")
                    ? WARN
                    : valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}
