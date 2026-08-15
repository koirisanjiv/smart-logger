# Smart Logger 0.3.4

Smart Logger is the centralized logging foundation for the Qaverse Smart automation ecosystem.

It gives application code one simple API while keeping destination control centralized:

- Console
- File
- Report integration
- Runtime enable/disable
- Log levels
- Semantic events: STEP, SUCCESS, FAILURE, TIMING
- Exception logging
- Lightweight timing

The production artifact is a normal Maven library. JMH benchmarks are intentionally **not** included in the client dependency.

## Coordinates

```xml
<dependency>
    <groupId>com.qaverse.smart</groupId>
    <artifactId>smart-logger</artifactId>
    <version>0.3.4</version>
</dependency>
```

The dependency requires Java 21 and brings Log4j2 API/Core transitively.

> Important: the coordinates above are the release coordinates. For a client to resolve `0.3.4` from a remote Maven repository, the artifact must first be published to that repository. This package contains everything required to build and publish it.

## Quick usage

```java
import com.qaverse.smart.logger.SmartLog;

SmartLog.info("Login started");
SmartLog.debug("DOM inspection");
SmartLog.step("Click Login");
SmartLog.success("Login successful");
SmartLog.failure("Login failed", exception);
```

Timing:

```java
try (var timer = SmartLog.startTimer("Login")) {
    // automation
}
```

Or:

```java
var timer = SmartLog.startTimer("Login");
// automation
long elapsedNanos = timer.stop();
```

## Runtime control

```java
import com.qaverse.smart.logger.SmartLogger;
import com.qaverse.smart.logger.SmartLogLevel;

SmartLogger.enableConsole();
SmartLogger.disableConsole();

SmartLogger.enableFile();
SmartLogger.disableFile();

SmartLogger.enableReport();
SmartLogger.disableReport();

SmartLogger.setLevel(SmartLogLevel.DEBUG);
```

## System properties

```text
smart.logger.console=true|false
smart.logger.file=true|false
smart.logger.report=true|false
smart.logger.level=TRACE|DEBUG|INFO|WARN|ERROR
smart.logger.file.dir=./Logs
```

Example:

```bash
mvn test \
  -Dsmart.logger.console=true \
  -Dsmart.logger.file=true \
  -Dsmart.logger.report=false \
  -Dsmart.logger.level=INFO \
  -Dsmart.logger.file.dir=./Logs
```

Defaults:

| Property | Default |
|---|---|
| `smart.logger.console` | `true` |
| `smart.logger.file` | `false` |
| `smart.logger.report` | `false` |
| `smart.logger.level` | `INFO` |
| `smart.logger.file.dir` | `./Logs` |

## Report integration

Smart Logger does not depend on Extent, Allure, Smart Reporting, or any client reporting framework.

Register a sink:

```java
SmartLogger.setReportSink(event -> {
    // Map SmartLogEvent to your reporting framework.
});
SmartLogger.enableReport();
```

The sink receives a `SmartLogEvent` containing:

- timestamp
- level
- message
- module
- thread
- event type
- duration
- throwable
- metadata

A report sink failure is isolated so a reporting integration cannot break the automation execution.


## Recreate the client distribution

After any production change, recreate the distributable package from the same project:

```bash
./distribution/create-distribution.sh
```

This runs the production test suite, builds the production JAR, and creates:

```text
distribution/smart-logger-<version>-distribution.zip
```

The distribution excludes JMH/benchmark code. The ZIP is useful for handing a release package to a client or for inspection; clients should ultimately consume the published Maven artifact.

Release flow:

```text
Change
  ↓
mvn clean test
  ↓
./distribution/create-distribution.sh
  ↓
GitHub commit + tag
  ↓
Publish Maven artifact
  ↓
Client pom.xml
```

## Distribution / publishing

### Local Maven repository

From this directory:

```bash
mvn clean test install
```

Then any local Maven project can use:

```xml
<dependency>
    <groupId>com.qaverse.smart</groupId>
    <artifactId>smart-logger</artifactId>
    <version>0.3.4</version>
</dependency>
```

### Remote Maven repository

Publish the `com.qaverse.smart:smart-logger:0.3.4` artifact to your organization's Maven repository or Maven Central, then clients only need the normal dependency declaration.

Do not put JMH in the client dependency. JMH belongs only to the benchmark project.

## Configuration ownership

Client/application code should normally use `SmartLog`.

Framework-level code may use `SmartLogger` to control destinations and integrations.

This separation is intentional:

```text
Application / Framework
        |
        v
     SmartLog
        |
        v
   SmartLogger
        |
        v
 SmartLoggerEngine
   |       |       |
Console   File   Report
```

## Files

- `README.md` — client-facing usage and distribution guide
- `readTechnicalStructure.md` — architecture and implementation details
- `pom.xml` — standalone Maven production artifact
- `src/main/java` — production code
- `src/main/resources/log4j2-smart-logger.xml` — default Log4j2 configuration

## Version

`0.3.4` — integration candidate / first distributable release.
