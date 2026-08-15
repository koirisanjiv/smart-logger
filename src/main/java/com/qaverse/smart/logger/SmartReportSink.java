package com.qaverse.smart.logger;

@FunctionalInterface
public interface SmartReportSink {
    void accept(SmartLogEvent event);
}
