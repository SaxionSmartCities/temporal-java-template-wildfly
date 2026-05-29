# Jakarta EE 11 Migration Plan (Wildfly 40)

This document outlines the analysis of Spring Framework usage in the project and provides a comprehensive checklist for migrating to Jakarta EE 11, specifically targeting **Wildfly 40**.

## 1. Analysis of Spring Framework Usage

The project currently uses Spring Boot 3.3.6 and several Spring-related libraries:

*   **Spring Boot Core**: `TemporalApplication.java` uses `@SpringBootApplication` and `SpringApplication.run`.
*   **Dependency Injection & Configuration**: `@Configuration`, `@Bean`, `@Value`, and `@Component` are used in `TemporalConfig.java`, `HttpActivitiesImpl.java`, and `CrawlerActivitiesImpl.java`.
*   **HTTP Client**: `RestTemplate` is used in `HttpActivitiesImpl.java` for making HTTP requests.
*   **Testing**: `@TestConfiguration` and Spring Boot test starters are used in `TestConfig.java` and various tests.
*   **Validation**: `spring-boot-starter-validation` (which wraps Jakarta Validation) is used in `build.gradle`.

## 2. Migration Goals

*   Replace Spring Boot with **Jakarta EE 11.0** (Target: **Wildfly 40**).
*   Change the build target from an executable JAR to a **WAR file**.
*   Replace Spring DI with **Jakarta CDI 4.1**.
*   Replace Spring `@Value` with **MicroProfile Config**.
*   Replace `RestTemplate` with **Jakarta REST Client 3.1**.
*   Update build system to use Jakarta EE 11 and MicroProfile dependencies.

## 3. Migration Checklist

### 3.1 Build Configuration (`build.gradle`)
- [x] Change `id 'org.springframework.boot'` to `id 'war'`.
- [x] Remove `io.spring.dependency-management` plugin.
- [x] Add Jakarta EE 11 API dependency: `compileOnly 'jakarta.platform:jakarta.jakartaee-api:11.0.0'`.
- [x] Add MicroProfile Config API: `compileOnly 'org.eclipse.microprofile.config:microprofile-config-api:3.1'`.
- [x] Remove Spring Boot starters.
- [x] Update `temporalVersion` if necessary to ensure compatibility.

### 3.2 Application & Packaging
- [x] Create `src/main/webapp/WEB-INF/web.xml` (optional in Jakarta EE, but good for defining the app).
- [x] Create `src/main/resources/META-INF/beans.xml` with `bean-discovery-mode="all"` or `annotated` to enable CDI 4.1.
- [x] Delete `TemporalApplication.java`.
- [x] Ensure `src/main/resources/application.yml` is migrated to `src/main/resources/META-INF/microprofile-config.properties`.

### 3.3 Dependency Injection & Configuration
- [x] Replace `@Component` with `@jakarta.enterprise.context.ApplicationScoped` for Activities.
- [x] Replace Spring's `@Configuration` and `@Bean` with CDI Producers in `TemporalConfig`.
- [x] Replace `@Value("${prop}")` with `@Inject @ConfigProperty(name = "prop")` (MicroProfile Config).
- [x] Replace `@Autowired` or Spring constructor injection with `@jakarta.inject.Inject`.
- [x] **Crucial**: Ensure Workflows remain deterministic and do NOT use CDI injection.

### 3.4 HTTP Client (`HttpActivitiesImpl.java`)
- [x] Replace `RestTemplate` with `jakarta.ws.rs.client.Client`.
- [x] Inject the client or use a Producer to manage its lifecycle.
- [x] Update `httpGet` implementation to use `jakarta.ws.rs.client.ClientBuilder`.

### 3.5 Temporal Configuration (`TemporalConfig.java`)
- [x] Convert to a CDI Producer class.
- [x] Use `@Produces` and `@ApplicationScoped` for `WorkflowServiceStubs` and `WorkflowClient`.
- [x] Use `@ConfigProperty` for `serviceAddress`, `namespace`, etc.

### 3.6 Workers (Management within Wildfly)
- [x] Since we are in a WAR, Workers should be started/stopped by the container.
- [x] Create a `@Startup @Singleton` bean (or a CDI observer for `Initialized(ApplicationScoped.class)`) to manage the `WorkerFactory` lifecycle.
- [x] Register Workflows and Activities within this lifecycle bean, injecting the CDI-managed Activity implementations.
- [x] Ensure `WorkerFactory.start()` is called on startup and `shutdown()` on teardown.

### 3.7 Testing
- [x] Replace Spring Test annotations with JUnit 5 + Arquillian or a lightweight CDI test runner (like Weld-JUnit).
- [x] Update `TestConfig.java` to a CDI-based test setup.
- [x] Ensure `TestWorkflowEnvironment` still functions correctly without Spring's context.

## 4. Risks and Considerations
*   **Determinism**: Injection is strictly forbidden in Workflow implementations. CDI proxies must not be used inside Workflows.
*   **Classloading**: Wildfly's modular classloading requires `jboss-deployment-structure.xml` to expose JDK internal modules like `jdk.management`, `jdk.unsupported`, and `sun.jdk` (used by Temporal SDK and its shaded gRPC/Netty dependencies).
*   **Shutdown Errors**: In modular environments like Wildfly, some classes required during shutdown (e.g., `ShutdownWorkerRequest$Builder`) might need to be pre-loaded during application startup to avoid `NoClassDefFoundError` when the classloader is being decommissioned.
*   **CDI Proxies and Background Threads**: Avoid using proxy-based CDI scopes (like `@ApplicationScoped`) for beans that are passed to and stored by external libraries (like Temporal SDK) which manage their own threads. While `@ApplicationScoped` can be used, ensure that the actual implementation instance (not a proxy) is passed to the SDK. In Jakarta EE, this can be achieved by using `Instance<T>.get()` or by choosing `@Dependent` scope. Passing a proxy can lead to `ContextNotActiveException` when background threads attempt to access the bean.
*   **Worker Threads**: Ensure Temporal workers don't conflict with Wildfly's thread management. Prefer using managed threads if required by the environment.
*   **Temporal Version**: Verify that the shaded Netty/gRPC used by Temporal SDK 1.25.2 plays well with Wildfly 40.
