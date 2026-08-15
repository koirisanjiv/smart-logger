package com.qaverse.smart.logger;

/** Mutable runtime configuration shared by the Smart Logger facade and engine. */
public final class SmartLoggerConfig {
    private volatile boolean consoleEnabled;
    private volatile boolean fileEnabled;
    private volatile boolean reportEnabled;
    private volatile SmartLogLevel level;
    private volatile String fileDirectory;

    private SmartLoggerConfig() {
        reload();
    }

    public static SmartLoggerConfig fromSystemProperties() {
        return new SmartLoggerConfig();
    }

    public synchronized void reload() {
        consoleEnabled = propertyBoolean("smart.logger.console", true);
        fileEnabled = propertyBoolean("smart.logger.file", false);
        reportEnabled = propertyBoolean("smart.logger.report", false);
        level = SmartLogLevel.from(System.getProperty("smart.logger.level"), SmartLogLevel.INFO);
        fileDirectory = System.getProperty("smart.logger.file.dir", "./Logs");
    }

    private boolean propertyBoolean(String key, boolean fallback) {
        String value = System.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    public boolean isConsoleEnabled() { return consoleEnabled; }
    public boolean isFileEnabled() { return fileEnabled; }
    public boolean isReportEnabled() { return reportEnabled; }
    public SmartLogLevel getLevel() { return level; }
    public String getFileDirectory() { return fileDirectory; }

    /** @deprecated use getFileDirectory(). */
    @Deprecated
    public String getFilePath() { return fileDirectory + "/Automation.log"; }

    public void setConsoleEnabled(boolean value) { consoleEnabled = value; }
    public void setFileEnabled(boolean value) { fileEnabled = value; }
    public void setReportEnabled(boolean value) { reportEnabled = value; }
    public void setLevel(SmartLogLevel value) { level = value == null ? SmartLogLevel.INFO : value; }
    public void setFileDirectory(String value) {
        fileDirectory = value == null || value.isBlank() ? "./Logs" : value;
    }

    /** @deprecated use setFileDirectory(). */
    @Deprecated
    public void setFilePath(String value) {
        if (value == null || value.isBlank()) {
            fileDirectory = "./Logs";
            return;
        }
        int separator = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        fileDirectory = separator > 0 ? value.substring(0, separator) : value;
    }
}
