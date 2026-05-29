package com.example.temporal.workflows.http;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of HTTP activities using Jakarta REST Client. */
@ApplicationScoped
public class HttpActivitiesImpl implements HttpActivities {

  private static final Logger logger = LoggerFactory.getLogger(HttpActivitiesImpl.class);

  private final Client client;

  /** Default constructor that creates a Jakarta REST Client instance. */
  public HttpActivitiesImpl() {
    this.client = ClientBuilder.newClient();
  }

  /**
   * Constructor with Client injection.
   *
   * @param client The Client to use for HTTP requests
   */
  @Inject
  public HttpActivitiesImpl(Client client) {
    this.client = client;
  }

  @Override
  public HttpGetActivityOutput httpGet(HttpGetActivityInput input) {
    logger.info("Performing HTTP GET request to URL: {}", input.url());

    try (Response response = client.target(input.url()).request().get()) {
      String responseText = response.readEntity(String.class);
      int statusCode = response.getStatus();

      logger.info("HTTP GET request completed with status code: {}", statusCode);

      return new HttpGetActivityOutput(responseText, statusCode);
    } catch (Exception e) {
      logger.error("Error performing HTTP GET request to {}: {}", input.url(), e.getMessage());
      throw e;
    }
  }
}
