package com.example.temporal.config;

import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for Temporal workflow client beans.
 *
 * <p>This configuration automatically detects whether to connect to a local Temporal server or
 * Temporal Cloud based on the presence of mTLS certificate configuration. If cert-path and key-path
 * are provided, it enables mTLS for Temporal Cloud. Otherwise, it connects to a local Temporal
 * server.
 */
@ApplicationScoped
public class TemporalConfig {

  private static final Logger logger = LoggerFactory.getLogger(TemporalConfig.class);

  @Inject
  @ConfigProperty(name = "temporal.service-address")
  private String serviceAddress;

  @Inject
  @ConfigProperty(name = "temporal.namespace")
  private String namespace;

  @Inject
  @ConfigProperty(name = "temporal.cert-path")
  private Optional<String> certPath;

  @Inject
  @ConfigProperty(name = "temporal.key-path")
  private Optional<String> keyPath;

  /**
   * Creates a WorkflowServiceStubs bean for connecting to the Temporal server.
   *
   * <p>Automatically configures mTLS if cert-path and key-path are provided, otherwise uses
   * standard connection for local development.
   *
   * @return WorkflowServiceStubs configured with the service address and optional mTLS
   * @throws Exception if mTLS configuration fails
   */
  @Produces
  @ApplicationScoped
  public WorkflowServiceStubs workflowServiceStubs() throws Exception {
    WorkflowServiceStubsOptions.Builder optionsBuilder =
        WorkflowServiceStubsOptions.newBuilder().setTarget(serviceAddress);

    // Enable mTLS if cert paths are provided (Temporal Cloud)
    if (certPath.isPresent() && keyPath.isPresent()) {
      logger.info("Configuring Temporal Cloud connection with mTLS to: {}", serviceAddress);

      try (InputStream clientCert = new FileInputStream(certPath.get());
          InputStream clientKey = new FileInputStream(keyPath.get())) {

        SslContext sslContext =
            GrpcSslContexts.forClient().keyManager(clientCert, clientKey).build();

        optionsBuilder.setSslContext(sslContext);
      }

      logger.info("mTLS configuration successful");
    } else {
      logger.info("Configuring local Temporal server connection to: {}", serviceAddress);
    }

    return WorkflowServiceStubs.newServiceStubs(optionsBuilder.build());
  }

  /**
   * Creates a WorkflowClient bean for executing workflows.
   *
   * @param workflowServiceStubs the service stubs to use for communication
   * @return WorkflowClient configured with the specified namespace
   */
  @Produces
  @ApplicationScoped
  public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
    WorkflowClientOptions options =
        WorkflowClientOptions.newBuilder().setNamespace(namespace).build();
    return WorkflowClient.newInstance(workflowServiceStubs, options);
  }

  /**
   * Creates a Jakarta REST Client bean.
   *
   * @return Jakarta REST Client
   */
  @Produces
  @ApplicationScoped
  public Client httpClient() {
    return ClientBuilder.newClient();
  }

  /**
   * Disposes the Jakarta REST Client.
   *
   * @param client the client to dispose
   */
  public void closeHttpClient(@Disposes Client client) {
    if (client != null) {
      client.close();
    }
  }

  /**
   * Disposes the WorkflowServiceStubs.
   *
   * @param stubs the stubs to dispose
   */
  public void closeWorkflowServiceStubs(@Disposes WorkflowServiceStubs stubs) {
    if (stubs != null) {
      stubs.shutdown();
    }
  }
}
