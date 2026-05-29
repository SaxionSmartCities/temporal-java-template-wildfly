package com.example.temporal.config;

import com.example.temporal.api.HttpsCallsResource;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;

/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Jakarta EE "no
 * XML" approach to activating JAX-RS.
 *
 * <p>
 *
 * <p>Resources are served relative to the servlet path specified in the {@link ApplicationPath}
 * annotation.
 */
@ApplicationPath("/api")
@ApplicationScoped
public class RestApplication extends Application {
  @Inject private Logger log;

  @PostConstruct
  public void postConstruct() {
    StringBuilder builder = new StringBuilder();
    builder.append("\n------------------------------------------------");
    builder.append("\nStarting up Temporal Rest Application");
    builder.append("\n------------------------------------------------");
    log.info(builder.toString());
  }

  /**
   * When an REST interface is defined through an API, the scanning does not work. Lists the classes
   * explicitly.
   */
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> resources = new HashSet<>();
    resources.add(HttpsCallsResource.class);
    return resources;
  }
}
