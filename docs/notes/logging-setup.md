## Logging setup for TestNG automation

This note explains recommended logging packages, why to use them, and step-by-step guidance to add per-test logging for TestNG tests in this repository. It includes code snippets and a line-by-line explanation so you can implement the changes safely.

---

## Summary recommendation

- Use SLF4J as the logging API and Logback Classic as the implementation.
  - SLF4J gives a stable API and lets you swap implementations later.
  - Logback supports SiftingAppender (per-MDC routing), AsyncAppender, Rolling policies, and integrates well with test lifecycles.
- Optionally add `logstash-logback-encoder` if you want JSON structured logs.

Why this choice?
- Familiar, well-documented, low friction to add to Maven projects.
- Enables per-test log files (very useful when tests run in parallel).
- Good support for MDC which we will use to tag logs with a test id.

## What we will achieve (concrete)

- Add dependencies (SLF4J + Logback) to the project POM (I will prepare the exact block on request).
- Add `src/test/resources/logback-test.xml` that:
  - Emits a console log for CI readability.
  - Writes per-test log files to `target/logs/test-${testId}.log` using Logback `SiftingAppender` keyed by MDC `testId`.
  - Uses `AsyncAppender` to avoid IO blocking on the test thread.
  - Uses `RollingPolicy` to cap log file sizes.
- Instrument `BaseTest` (TestNG) and `Hooks` (Cucumber) to set MDC.put("testId", ...) at test start and remove it at the end.
- On failure attach the per-test log file to Allure report.

## High-level design (TestNG)

- TestNG: set MDC in `@BeforeMethod` using the TestNG method metadata and clear it in `@AfterMethod`. Attach logs on failure in `@AfterMethod`.

## Dependencies (Maven) — conceptual

Add these dependencies to `pom.xml` under `<dependencies>` (I will not modify files unless you ask):

```xml

<!-- SLF4J API: lightweight facade -->
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-api</artifactId>
  <version>1.7.36</version>
</dependency>

<!-- Logback Classic: SLF4J implementation -->
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.2.11</version>
</dependency>

<!-- Optional: JSON encoder for structured logs -->
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.2</version>
</dependency>
```

Line-by-line explanation (dependencies):
- `slf4j-api`: the logging API used from your Java code (calls like `logger.info(...)`). Using an API decouples code from implementation.
- `logback-classic`: the runtime implementation that actually writes logs to files/console. It implements SLF4J.
- `logstash-logback-encoder`: optional — if you want JSON output (useful for centralized log ingestion).

## `logback-test.xml` example

Create `src/test/resources/logback-test.xml` with the following (drop-in) configuration. This example creates per-test files keyed by MDC `testId`, rotates them, and writes to console. It's safe to start with and then fine-tune.

```xml
<configuration>
  <!-- Console appender for readability in CI logs -->
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- RollingFile that will be wrapped by sifting (per-test) -->
  <appender name="PER_TEST_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>target/logs/test.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <!-- rotate daily + keep 7 days -->
      <fileNamePattern>target/logs/test.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
      <maxHistory>7</maxHistory>
      <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>10MB</maxFileSize>
      </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
    <encoder>
      <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <!-- SiftingAppender routes logs into a per-test rolling file, using MDC 'testId' as key -->
  <appender name="SIFT" class="ch.qos.logback.classic.sift.SiftingAppender">
    <discriminator>
      <key>testId</key>
      <defaultValue>unknown</defaultValue>
      <class>ch.qos.logback.classic.sift.MDCBasedDiscriminator</class>
    </discriminator>
    <sift>
      <appender name="FILE-${testId}" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>target/logs/test-${testId}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
          <fileNamePattern>target/logs/test-${testId}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
          <maxHistory>5</maxHistory>
          <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>5MB</maxFileSize>
          </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
          <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
      </appender>
    </sift>
  </appender>

  <!-- Async wrapper to reduce blocking on IO -->
  <appender name="ASYNC_SIFT" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="SIFT" />
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
  </appender>

  <!-- Root logger: console + sifting/async -->
  <root level="INFO">
    <appender-ref ref="CONSOLE" />
    <appender-ref ref="ASYNC_SIFT" />
  </root>
</configuration>
```

Line-by-line explanation of the important parts:
- `<ConsoleAppender>`: writes log lines to STDOUT so when Maven/CI runs, the logs are visible.
- `PER_TEST_FILE` (example): a rolling file to show base pattern — not used directly by sifting but useful as a fallback.
- `SiftingAppender`: the core of per-test logs. It inspects MDC key `testId` and creates a dedicated appender (`FILE-${testId}`) for each distinct `testId` value.
  - `discriminator` tells the sifting appender to use `MDC.get("testId")` as the routing key.
  - inside `<sift>` we create a `RollingFileAppender` for `target/logs/test-${testId}.log`.
- `AsyncAppender` wraps `SIFT` to buffer log events and write asynchronously. This keeps tests from being slowed by synchronous disk IO.
- `root logger` references console and the async-sift so every logging call goes to both.

Notes / safety:
- `target/logs` should be added to .gitignore (usually it's already ignored). The RollingPolicy protects disk growth.
- Keep `maxHistory` and `maxFileSize` conservative to avoid filling disk in CI.

## Test code changes (snippets + explanations)

Below are minimal code snippets you will need in `BaseTest` (TestNG). They are short and safe to add.

1) TestNG (`BaseTest`) — set MDC per test and attach on failure

```java
import org.slf4j.MDC;
import java.lang.reflect.Method;
import java.util.UUID;

@BeforeMethod(alwaysRun = true)
public void beforeMethod(Method method) {
    // Create a stable-enough test id: className-methodName-UUID
    String testId = method.getDeclaringClass().getSimpleName() + "-" + method.getName() + "-" + UUID.randomUUID();
    MDC.put("testId", testId);
}

@AfterMethod(alwaysRun = true)
public void afterMethod(ITestResult result) {
    // attach per-test log file to Allure if available
    String testId = MDC.get("testId");
    if (testId != null) {
        Path log = Paths.get("target/logs/test-" + testId + ".log");
        if (Files.exists(log)) {
            byte[] bytes = Files.readAllBytes(log);
            io.qameta.allure.Allure.addAttachment("test-log", new ByteArrayInputStream(bytes));
        }
    }
    MDC.remove("testId");
}
```

Line-by-line explanation:
- `MDC.put("testId", ...)`: adds a key-value to the thread-local MDC so logback's `SiftingAppender` sees it and routes logs to the per-test file.
- `UUID.randomUUID()`: ensures uniqueness for repeated runs and parallel tests — you could instead use a deterministic id if desired.
- In `afterMethod`, we read the per-test log file and attach it to Allure using `Allure.addAttachment(...)`.
- Finally `MDC.remove("testId")` to avoid leakage into other tests.

Notes: use `Method` from `java.lang.reflect` to build a helpful name. For parallel TestNG runs ensure each thread sets its own MDC (MDC is thread-local by default).

2) (Optional) Browser console logs

Capture browser console logs and attach them to Allure on failure. This is separate from test-run logs but very helpful.

3) Browser console logs (complementary)

Capture browser console logs and attach them to Allure on failure. This is separate from test-run logs but very helpful.

```java
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
StringBuilder sb = new StringBuilder();
for (LogEntry e : entries) {
    sb.append(new Date(e.getTimestamp())).append(" ").append(e.getLevel()).append(" ").append(e.getMessage()).append("\n");
}
io.qameta.allure.Allure.addAttachment("browser-console.log", "text/plain", new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)), ".log");
```

Explanation:
- `LogType.BROWSER` reads browser console messages from the driver.
- We convert them to a single text blob and attach to Allure.

## Verification steps (manual)

1. Add the dependencies to `pom.xml` (I'll provide exact block if you want me to prepare it).
2. Add `src/test/resources/logback-test.xml` (copy the snippet above).
3. Add the MDC `put`/`remove` code to `BaseTest` and `Hooks` as shown.
4. Run a single test locally (e.g., a failing login test). After the run, inspect `target/logs/` — you should see `test-<id>.log`. Open the file to see test-scoped logs.
5. Open Allure results — the failing test should include the attached `test-log` and/or `browser-console.log`.

## Edge cases & production considerations

- Parallel tests: MDC is thread-local; SiftingAppender will create separate files per unique `testId`. Ensure your naming is unique when tests run in parallel.
- Disk usage: use `maxFileSize` and `maxHistory` in rolling policy. Optionally add a scheduled job to clean old logs.
- Sensitive data: don't log PII. Add log filters or avoid logging passwords/SSNs.
- Performance: AsyncAppender helps; tune queue size for large test suites.

## Minimal incremental approach (safe path)

1. Add SLF4J + logback-classic to pom.xml.
2. Create a simple `logback-test.xml` that only logs to console and a single rotating `target/test.log` — verify it works.
3. Add MDC wiring in `BaseTest` to set testId and see that `target/test.log` contains the `testId` in each line (update pattern to include `%X{testId}`).
4. When step 3 is stable, upgrade the config to use `SiftingAppender` to get per-test files.

## Minimal config snippet to show `testId` in lines (pattern)

In `logback-test.xml` use a pattern that prints MDC values:

```xml
<pattern>%d{ISO8601} [%thread] %-5level [%X{testId}] %logger{36} - %msg%n</pattern>
```

This will print the `testId` in square brackets for each log line so you can correlate logs with tests even before per-test files are enabled.

## Wrap-up

If you want I can next:

- Draft the exact dependency block to paste into `pom.xml`.
- Create the `src/test/resources/logback-test.xml` file with the SiftingAppender shown above.
- Prepare a patch for `BaseTest` to add MDC and Allure attachments.

Tell me which of the three you want me to prepare (I will not apply unless you confirm):
- A: draft POM dependencies only
- B: create `logback-test.xml` in `src/test/resources`
- C: produce a small patch for `BaseTest` showing the exact changes
- D: All of the above as a single patch set

If you're unsure, pick A then B. We'll take the minimal incremental path.

---

File created by automation assistant on request: concise, stepwise, and intended to be copy-paste ready.
