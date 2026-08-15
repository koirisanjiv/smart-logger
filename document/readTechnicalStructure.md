# Smart Logger 0.3.4 — Technical Structure

## 1. Purpose

Smart Logger is the centralized logging layer for:

```text
Smart Core
Smart Field Control
Smart Reporting
Smart Trace
Client Projects
```

The design goal is:

> application code should use one simple logging API, while destination routing and integration behavior remain centrally controlled.

The logger is intentionally independent from higher-level Smart modules.

---

## 2. Module boundary

The distributable artifact contains only:

```text
com.qaverse.smart:smart-logger:0.3.4
```

It does **not** contain:

- Smart Core
- Smart Field Control
- Smart Reporting
- Smart Trace
- JMH benchmark classes
- client-specific reporting implementations

The production dependency contains Log4j2 API/Core as runtime dependencies.

Benchmark code is intentionally kept outside the production artifact.

---

## 3. Package structure

```text
src/main/java/
└── com/qaverse/smart/logger/
    ├── SmartLog.java
    ├── SmartLogger.java
    ├── SmartLoggerConfig.java
    ├── SmartLogLevel.java
    ├── SmartLogEvent.java
    ├── SmartLogEventType.java
    ├── SmartReportSink.java
    ├── SmartTimer.java
    └── internal/
        └── SmartLoggerEngine.java

src/main/resources/
└── log4j2-smart-logger.xml
```

---

## 4. Public API

### SmartLog

`SmartLog` is the normal application-facing facade.

Supported operations:

```text
trace()
debug()
info()
warn()
error()
error(message, throwable)

step()
success()
failure(message, throwable)

startTimer()
timed()

isTraceEnabled()
isDebugEnabled()
isInfoEnabled()
```

Most client code should only need `SmartLog`.

Example:

```java
SmartLog.debug("Finding login button");
SmartLog.step("Click Login");
SmartLog.success("Login successful");
```

---

## 5. Runtime controller

`SmartLogger` controls global logger behavior.

Responsibilities:

```text
Configuration access
Reload system properties
Console enable/disable
File enable/disable
Report enable/disable
Log level
Report sink registration
```

Example:

```java
SmartLogger.enableConsole();
SmartLogger.enableFile();
SmartLogger.disableReport();
SmartLogger.setLevel(SmartLogLevel.DEBUG);
```

The controller uses atomic references for global configuration/sink replacement.

---

## 6. Configuration

`SmartLoggerConfig` contains mutable runtime configuration:

```text
consoleEnabled
fileEnabled
reportEnabled
level
fileDirectory
```

System properties are read with these keys:

```text
smart.logger.console
smart.logger.file
smart.logger.report
smart.logger.level
smart.logger.file.dir
```

Default values:

```text
console = true
file = false
report = false
level = INFO
directory = ./Logs
```

Configuration is held using volatile fields so runtime destination changes are visible across threads.

---

## 7. Fast path

The most important performance path is handled in `SmartLoggerEngine`.

First check:

```text
Does the configured level allow this event?
```

If not:

```text
return
```

No event object is created.

No console work.

No file work.

No report work.

Then:

```text
Are all destinations disabled?
```

If yes:

```text
return
```

Again, no unnecessary event creation.

Conceptually:

```text
SmartLog.debug()
      |
      v
level allowed?
      |
     NO -----> return
      |
     YES
      |
      v
any destination?
      |
     NO -----> return
      |
     YES
      |
      v
route event
```

This is why disabled DEBUG and disabled-destination paths are intentionally kept extremely small.

---

## 8. Console and file routing

The engine maintains dedicated Log4j2 loggers:

```text
smart.console
smart.file
```

The Log4j2 configuration controls their appenders.

This separation allows:

```text
Console ON
File OFF

Console OFF
File ON

Console ON
File ON
```

without changing application code.

---

## 9. Report architecture

Smart Logger does not know about Extent or any other reporting implementation.

It exposes:

```java
@FunctionalInterface
public interface SmartReportSink {
    void accept(SmartLogEvent event);
}
```

The reporting layer registers an implementation:

```java
SmartLogger.setReportSink(event -> {
    // reporting integration
});
```

Then:

```java
SmartLogger.enableReport();
```

The logger creates a `SmartLogEvent` only when the report destination is enabled and a sink exists.

---

## 10. SmartLogEvent

`SmartLogEvent` is the semantic event model used by reporting/integrations.

Fields:

```text
timestamp
level
message
module
threadName
eventType
durationNanos
throwable
metadata
```

Event types:

```text
LOG
STEP
SUCCESS
FAILURE
TIMING
```

This is the main integration boundary between Smart Logger and Smart Reporting/Trace.

---

## 11. Exception isolation

Report integrations are external to the logger core.

Therefore:

```text
Report sink throws RuntimeException
        |
        v
Smart Logger catches it
        |
        v
Automation continues
```

If file logging is enabled, the sink failure is additionally written at DEBUG level to the file logger.

The reporting layer must never become a reason for an automation test to fail.

---

## 12. Timing

`SmartTimer` uses:

```java
System.nanoTime()
```

for elapsed duration.

Example:

```java
try (var timer = SmartLog.startTimer("Login")) {
    login();
}
```

On close/stop:

```text
elapsed nanos
      |
      v
SmartLog.timed()
      |
      v
TIMING event
```

Timer stop is guarded by an atomic flag so it is emitted only once.

---

## 13. Log4j2 configuration

The bundled configuration is:

```text
src/main/resources/log4j2-smart-logger.xml
```

It provides the destination foundation.

The normal file destination is:

```text
./Logs/Automation.log
```

Rolling files are used to prevent one unbounded log file.

Client applications can supply their own Log4j2 configuration when required, as long as the Smart Logger logger names are mapped correctly.

Important logger names:

```text
smart.console
smart.file
```

---

## 14. Dependency graph

Production:

```text
Client
  |
  +--> smart-logger:0.3.4
          |
          +--> log4j-api:2.26.1
          |
          +--> log4j-core:2.26.1
```

The client does NOT receive:

```text
JMH
benchmark code
benchmark resources
```

---

## 15. Performance validation

Validated benchmark results from the development cycle:

```text
DEBUG disabled
≈ 1.633 ns/op

INFO + all destinations disabled
≈ 2.257 ns/op

Report-only path
≈ 59.996 ns/op
```

These numbers describe the tested JDK 21/JVM environment and should not be treated as universal guarantees.

File persistence was separately verified with real records and rolling files.

File throughput was intentionally not promoted as a fixed product guarantee because filesystem, rollover, compression, buffering, and machine load materially affect it.

---

## 16. Thread safety

Global configuration is held through atomic references.

Configuration fields are volatile.

The report sink is stored atomically.

The timing guard uses `AtomicBoolean`.

The engine itself does not keep per-test mutable state.

Therefore Smart Logger is designed to be safe for parallel automation execution.

---

## 17. Integration rule for Smart ecosystem

Higher-level modules should depend downward:

```text
Smart Field Control
        |
Smart Core / Smart Reporting / Smart Trace
        |
        v
   Smart Logger
```

Smart Logger must NOT depend upward on those modules.

This prevents circular dependencies and keeps the logger reusable in client projects.

---

## 18. Recommended usage policy

Application/framework developers:

```java
SmartLog.info(...)
SmartLog.debug(...)
SmartLog.step(...)
SmartLog.success(...)
SmartLog.failure(...)
```

Framework owners/configuration:

```java
SmartLogger.enableConsole()
SmartLogger.enableFile()
SmartLogger.enableReport()
SmartLogger.setLevel(...)
SmartLogger.setReportSink(...)
```

Do not use `System.out.println()` for normal framework diagnostics once Smart Logger is integrated.

---

## 19. Future extension points

Possible future work, kept outside the 0.3.4 baseline:

```text
Async file pipeline
Structured JSON logging
Correlation/test/session IDs
Automatic context propagation
Sensitive-data masking
Log sampling
Dynamic configuration provider
Smart Trace integration
Performance counters
```

These should be added only when a real Smart ecosystem requirement justifies them.

---

## 20. Release principle

Smart Logger should remain:

```text
small
fast
centralized
dependency-light
framework-independent
safe under parallel execution
```

The logger is infrastructure. It should not become a second automation framework.


---

## 21. Distribution and publishing architecture

Production source and publishing are intentionally separated from benchmark code:

```text
smart-logger
    |
    +-- production library
    |
    +-- tests
    |
    +-- distribution scripts
    |
    +-- GitHub Actions
              |
              v
        Maven Central
```

The benchmark module is never part of the client dependency.

### Release pipeline

```text
Developer changes
      |
      v
mvn clean test
      |
      v
./distribution/release.sh X.Y.Z
      |
      +--> update version
      +--> test
      +--> create distribution ZIP
      +--> git tag vX.Y.Z
      |
      v
git push origin main --follow-tags
      |
      v
GitHub Actions
      |
      +--> validate tag/version
      +--> test
      +--> GPG sign
      +--> Central deploy
      |
      v
Maven Central
```

Publishing credentials are GitHub repository secrets and are not stored in source control.

The Central Publisher Portal's official Maven plugin supports automatic publishing with `autoPublish=true`. citeturn0search0
