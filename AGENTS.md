# Temporal Java Template - AI Assistant Guide

This document provides comprehensive guidance for AI assistants (like Claude, GitHub Copilot, or ChatGPT) working with this Temporal Java SDK project template.

## Overview

This is a **Temporal Java SDK project template** designed for building reliable, distributed applications using modern Java practices. The template demonstrates core Temporal patterns through two practical workflows (HTTP and Crawler) while maintaining production-grade code quality standards.

**Primary Use Case**: Starting point for AI-enabled workflow development in Java, with emphasis on:
- Type-safe workflow orchestration
- Comprehensive testing patterns
- Enterprise-grade code quality
- Jakarta EE 11 integration

## Tech Stack

Understanding these technologies is essential for working with this codebase:

### Core Technologies
- **Temporal SDK (1.35.0)**: Orchestration engine for building resilient distributed systems
- **Java 21**: LTS version with modern language features (records, pattern matching, virtual threads)
- **Jakarta EE 11**: Enterprise application framework (CDI, Jakarta REST)
- **Wildfly 40.0.0.Final**: Application server implementing Jakarta EE 11 
- **Gradle 9.5.1**: Build automation with Groovy DSL

### Data & HTTP
- **Jackson**: JSON serialization/deserialization (Temporal's default)
- **Java Records**: Immutable data models for workflow inputs/outputs
- **Jakarta REST Client**: HTTP client for activities
- **Jakarta Validation**: Input validation annotations

### Code Quality
- **Checkstyle**: Google Java Style enforcement
- **SpotBugs**: Static analysis for bug detection
- **Spotless**: Automatic code formatting (Google Java Format)
- **JaCoCo**: Code coverage reporting (80% minimum)

### Testing
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework for unit tests
- **TestWorkflowEnvironment**: Temporal's in-memory testing with time-skipping

### Development Tools
- **Pre-commit**: Git hooks for code quality
- **Gradle Toolchains**: Automatic JDK provisioning

## Project Structure

```
temporal-java-template/
├── src/
│   ├── main/
│   │   ├── java/com/example/temporal/
│   │   │   ├── config/
│   │   │   │   ├── RestApplication.java                 # Jakarta EE Rest Application
│   │   │   │   ├── Resources.java                       # CDI Producers
│   │   │   │   ├── TemporalConfig.java                  # Temporal client beans
│   │   │   │   └── TemporalWorkerManager.java           # Worker lifecycle management
│   │   │   ├── api/                                     # REST Endpoints
│   │   │   │   └── *.java
│   │   │   └── workflows/
│   │   │       ├── http/                         # HTTP workflow package
│   │   │       │   ├── HttpWorkflow.java         # Workflow interface
│   │   │       │   ├── HttpWorkflowImpl.java     # Workflow implementation
│   │   │       │   ├── HttpActivities.java       # Activity interface
│   │   │       │   ├── HttpActivitiesImpl.java   # Activity implementation
│   │   │       │   ├── HttpWorker.java           # Standalone worker
│   │   │       │   └── *.java                    # Data models (records)
│   │   │       └── crawler/                      # Crawler workflow package
│   │   │           └── [similar structure]
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   ├── beans.xml                     # CDI enablement
│   │   │   │   └── microprofile-config.properties # MicroProfile configuration
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           └── jboss-deployment-structure.xml # Wildfly specific config
│   └── test/
│       └── java/com/example/temporal/
│           ├── TestConfig.java                   # Test environment setup
│           └── workflows/
│               ├── http/
│               │   ├── HttpActivitiesTest.java   # Activity unit tests
│               │   └── HttpWorkflowTest.java     # Workflow integration tests
│               └── crawler/
│                   └── [similar test structure]
├── config/
│   ├── checkstyle/checkstyle.xml                 # Code style rules
│   └── spotbugs/exclude.xml                      # Static analysis exclusions
├── docs/
│   ├── temporal-patterns.md                      # Temporal patterns guide
│   └── testing.md                                # Testing guide
├── build.gradle                                  # Gradle build configuration
├── gradle.properties                             # Build properties
├── settings.gradle                               # Gradle settings
├── .pre-commit-config.yaml                       # Pre-commit hooks
├── README.md                                     # User documentation
└── AGENTS.md                                     # This file

```

## Key Architectural Concepts

### Temporal's Separation of Concerns

1. **Workflows** (`*Workflow.java`, `*WorkflowImpl.java`)
   - Orchestrate business logic
   - Must be **deterministic** (same inputs → same outputs)
   - Cannot perform I/O directly
   - Use `Workflow.*` methods for time, random, logging
   - Serialized and replayed during execution

2. **Activities** (`*Activities.java`, `*ActivitiesImpl.java`)
   - Execute non-deterministic operations (HTTP, database, file I/O)
   - Can fail and retry automatically
   - Injected with Jakarta CDI-beans
   - Use standard logging (`LoggerFactory.getLogger()`)

3. **Workers** (`*Worker.java`)
   - Poll task queues and execute workflows/activities
   - Run preferably separately from Wildfly. For convenience run inside Wildfly
   - Configure thread pools for concurrent execution
   - Production workers often run in separate processes/containers

4. **Data Models** (Java Records)
   - Immutable, serializable workflow inputs/outputs
   - Type-safe contract between workflow and client
   - Jackson-compatible by default

### Workflow Determinism Rules

**✅ ALLOWED in Workflows:**
```java
Workflow.currentTimeMillis()        // Deterministic time
Workflow.newRandom()                 // Deterministic random
Workflow.getLogger()                 // Replay-safe logging
Workflow.sleep(Duration)             // Workflow timers
Workflow.await(() -> condition)      // Wait for signals
activities.doSomething()             // Activity calls
Async.function(activities::method)   // Parallel execution
```

**❌ FORBIDDEN in Workflows:**
```java
System.currentTimeMillis()           // Non-deterministic!
Math.random()                        // Non-deterministic!
new Random().nextInt()               // Non-deterministic!
LoggerFactory.getLogger()            // Wrong logger!
Thread.sleep()                       // Blocks workflow!
client.target(url).request().get()   // Direct I/O forbidden!
repository.save()                    // Direct I/O forbidden!
```

## Development Standards

### Code Quality Requirements

All code must pass these checks before commit:

1. **Spotless Check** (`./gradlew spotlessCheck`)
   - Google Java Format (2-space indentation)
   - Import ordering
   - Trailing whitespace removal
   - Fix automatically: `./gradlew spotlessApply`

2. **Checkstyle** (`./gradlew checkstyleMain checkstyleTest`)
   - Google Java Style with modifications
   - 2-space indentation
   - Lowercase `logger` constant allowed
   - Test methods can use underscores: `test_method_scenario()`

3. **SpotBugs** (`./gradlew spotbugsMain`)
   - Static analysis for common bugs
   - Excludes: EI_EXPOSE_REP for Temporal patterns
   - View report: `build/reports/spotbugs/main/spotbugs.html`

4. **Test Coverage** (`./gradlew test jacocoTestReport`)
   - Minimum 80% code coverage
   - Both unit tests (activities) and integration tests (workflows)
   - View report: `build/reports/jacoco/test/html/index.html`

### Coding Conventions

**Data Models - Prefer Records:**
```java
// ✅ GOOD: Immutable record
public record HttpWorkflowInput(String url) {}

public record HttpWorkflowOutput(
    String responseText,
    String url,
    int statusCode
) {}
```

**Activity Interface & Implementation:**
```java
// Activity interface
@ActivityInterface
public interface MyActivities {
  MyOutput doSomething(MyInput input);
}

// Activity implementation (Jakarta EE component)
@ApplicationScoped
public class MyActivitiesImpl implements MyActivities {
  private static final Logger logger = LoggerFactory.getLogger(MyActivitiesImpl.class);
  private final Client httpClient;

  @Inject
  public MyActivitiesImpl(Client httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public MyOutput doSomething(MyInput input) {
    logger.info("Processing: {}", input);
    // Implementation with I/O operations using Jakarta REST Client
    try (Response response = httpClient.target("https://api.example.com").request().get()) {
       return new MyOutput(response.readEntity(String.class));
    }
  }
}
```

**Workflow Interface & Implementation:**
```java
// Interface
@WorkflowInterface
public interface MyWorkflow {
  @WorkflowMethod
  MyOutput run(MyInput input);
}

// Implementation
public class MyWorkflowImpl implements MyWorkflow {
  private static final Logger logger = Workflow.getLogger(MyWorkflowImpl.class);

  private final ActivityOptions activityOptions =
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(10))
          .build();

  private final MyActivities activities =
      Workflow.newActivityStub(MyActivities.class, activityOptions);

  @Override
  public MyOutput run(MyInput input) {
    logger.info("Starting workflow with input: {}", input);
    return activities.doSomething(input);
  }
}
```

**Worker:**
Note: When started by Wildfly, the workers are started by the TemporalWorkerManager.

```java
public class MyWorker {
  public static final String TASK_QUEUE = "my-task-queue";

  public static void main(String[] args) {
    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    worker.registerActivitiesImplementations(new MyActivitiesImpl());

    factory.start();
    logger.info("Worker started for task queue: {}", TASK_QUEUE);
  }
}
```

### Testing Patterns

**Unit Test (Activities):**
```java
@ExtendWith(MockitoExtension.class)
class MyActivitiesTest {
  @Mock private Client httpClient;
  @Mock private WebTarget webTarget;
  @Mock private Invocation.Builder builder;
  @Mock private Response response;
  private MyActivitiesImpl activities;

  @BeforeEach
  void setUp() {
    activities = new MyActivitiesImpl(httpClient);
  }

  @Test
  void testDoSomething_Success() {
    // Arrange
    when(httpClient.target(anyString())).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    when(response.readEntity(String.class)).thenReturn("mocked response");

    // Act
    MyOutput output = activities.doSomething(new MyInput("test"));

    // Assert
    assertNotNull(output);
    verify(httpClient).target(anyString());
  }
}
```

**Integration Test (Workflows):**
```java
class MyWorkflowTest {
  private TestWorkflowEnvironment testEnv;
  private Worker worker;
  private WorkflowClient client;

  @BeforeEach
  void setUp() {
    testEnv = TestWorkflowEnvironment.newInstance();
    worker = testEnv.newWorker(MyWorker.TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(MyWorkflowImpl.class);
    client = testEnv.getWorkflowClient();
  }

  @AfterEach
  void tearDown() {
    testEnv.close();
  }

  @Test
  void testWorkflow_Success() {
    // Mock activities
    MyActivities mockActivities = mock(MyActivities.class);
    when(mockActivities.doSomething(any())).thenReturn(new MyOutput("mocked"));
    worker.registerActivitiesImplementations(mockActivities);

    testEnv.start();

    // Execute workflow
    MyWorkflow workflow = client.newWorkflowStub(
        MyWorkflow.class,
        WorkflowOptions.newBuilder().setTaskQueue(MyWorker.TASK_QUEUE).build());

    MyOutput output = workflow.run(new MyInput("test"));

    // Assert
    assertEquals("mocked", output.result());
    verify(mockActivities).doSomething(any());
  }
}
```

## AI Assistant Guidance

### When Working with This Codebase

1. **Understand Temporal Concepts**
   - Workflows orchestrate, activities execute I/O
   - Workflows must be deterministic (no direct I/O, use `Workflow.*` methods)
   - Activities can fail and retry based on retry policies
   - Workers run separately from the Jakarta EE application

2. **Follow Existing Patterns**
   - Use Java records for data models (not POJOs with Lombok)
   - Activities are CDI components (`@ApplicationScoped`) with dependency injection
   - Workflows have NO Jakarta EE dependencies (they're serialized/replayed)
   - Package by feature: each workflow lives in its own package

3. **Code Organization**
   - Each workflow gets its own package under `workflows/`
   - Package includes: interfaces, implementations, data models, worker, tests
   - Data models use records with validation annotations if needed
   - Workers are standalone main classes (run separately) or (in Wildfly) started in `TemporalWorkerManager`.

4. **Testing Requirements**
   - Write unit tests for activities (mock external dependencies)
   - Write integration tests for workflows (mock activities)
   - Use `TestWorkflowEnvironment` for time-skipping tests
   - Maintain 80% code coverage minimum
   - Test both success and failure scenarios

5. **Error Handling**
   - Activities throw exceptions that trigger retries
   - Configure retry policies in `ActivityOptions`
   - Non-retryable exceptions: use `setDoNotRetry()`
   - Workflows handle `ActivityFailure` for compensation logic

6. **Documentation**
   - Update README.md for new workflows
   - Add examples to `docs/temporal-patterns.md`
   - Include test examples in `docs/testing.md`
   - Document complex business logic in workflow comments

### Common Tasks

**Adding a New Workflow:**

1. Create package: `src/main/java/com/example/temporal/workflows/myworkflow/`
2. Create data models (records): `MyWorkflowInput`, `MyWorkflowOutput`, etc.
3. Create activity interface: `MyActivities.java` with `@ActivityInterface`
4. Create activity implementation: `MyActivitiesImpl.java` as Jakarta `@ApplicationScoped`
5. Create workflow interface: `MyWorkflow.java` with `@WorkflowInterface` and `@WorkflowMethod`
6. Create workflow implementation: `MyWorkflowImpl.java` (no Jakarta EE annotations!)
7. Create worker: `MyWorker.java` with main method and task queue constant or add an entry in the TemporalWorkerManager.
8. Create tests: `MyActivitiesTest.java` (unit) and `MyWorkflowTest.java` (integration)
9. Update README.md with usage examples
10. Run quality checks: `./gradlew qualityCheck`

**Adding Dependencies:**

Edit `build.gradle`:
```groovy
dependencies {
    implementation 'group:artifact:version'
    testImplementation 'group:test-artifact:version'
}
```

Then sync: `./gradlew build --refresh-dependencies`

**Running Workflows Locally:**

1. Start Temporal Server: `temporal server start-dev`
2. Run worker: `./gradlew runMyWorker` (add task to build.gradle)
3. Execute workflow via CLI or Java client
4. View workflow in Temporal Web UI: http://localhost:8233

**Code Formatting:**
```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Check style
./gradlew checkstyleMain checkstyleTest

# Run static analysis
./gradlew spotbugsMain

# Run all quality checks
./gradlew qualityCheck
```

## Important Notes for AI Assistants

### Temporal-Specific Constraints

1. **Never inject Jakarta EE beans into workflows**
   - Workflows are serialized and replayed
   - Only activity stubs can be used in workflows
   - Move all Jakarta EE dependencies to activities

2. **Always use Workflow.* methods in workflows**
   - Time: `Workflow.currentTimeMillis()`, `Workflow.sleep()`
   - Random: `Workflow.newRandom()`
   - Logging: `Workflow.getLogger()`
   - Async: `Async.function()`, `Promise.allOf()`

3. **Activity timeouts are critical**
   - Always set `StartToCloseTimeout`
   - Consider `HeartbeatTimeout` for long activities
   - Configure retry policies for transient failures

4. **Testing is different from traditional Jakarta EE**
   - Use `TestWorkflowEnvironment`, not `@Arquillian` or similar for workflows
   - Mock activities, not external services in workflow tests
   - Time-skipping allows testing long-running workflows instantly

### Build System

- **Gradle 9.5.1** with Groovy DSL (not Kotlin DSL)
- **Java 21 Toolchain**: Auto-provisioned via Foojay Resolver
- **Build tasks**: `build`, `test`, `qualityCheck`, `spotlessApply`
- **Worker tasks**: `runHttpWorker`, `runCrawlerWorker` (JavaExec tasks)

### File Organization

- Source: `src/main/java/com/example/temporal/`
- Tests: `src/test/java/com/example/temporal/`
- Resources: `src/main/resources/` (application.yml, microprofile-config.properties)
- Config: `config/checkstyle/`, `config/spotbugs/`
- Docs: `docs/` (patterns, testing guides)

## Common Pitfalls to Avoid

1. ❌ **Don't use `System.currentTimeMillis()` in workflows** → Use `Workflow.currentTimeMillis()`
2. ❌ **Don't inject Jakarta EE beans into workflow implementations** → Use activities instead
3. ❌ **Don't use `Thread.sleep()` in workflows** → Use `Workflow.sleep()`
4. ❌ **Don't use standard logger in workflows** → Use `Workflow.getLogger()`
5. ❌ **Don't forget activity timeouts** → Always set `StartToCloseTimeout`
6. ❌ **Don't test workflows with real activities** → Mock activities in workflow tests
7. ❌ **Don't skip code quality checks** → Run `./gradlew qualityCheck` before commit
8. ❌ **Don't modify workflow code without versioning** → Use `Workflow.getVersion()` for changes

## References

- [Temporal Java SDK Documentation](https://docs.temporal.io/dev-guide/java)
- [Temporal Best Practices](https://docs.temporal.io/kb)
- [Jakarta EE Documentation](https://jakarta.ee/specifications/platform/11/)
- [Project README](README.md) - User-facing documentation
- [docs/temporal-patterns.md](docs/temporal-patterns.md) - Pattern reference
- [docs/testing.md](docs/testing.md) - Testing guide

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
