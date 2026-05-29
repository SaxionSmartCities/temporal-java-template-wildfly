package com.example.temporal.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans */
// @Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
@ApplicationScoped
public class Resources {
  //    private static final ValidatorFactory validatorFactory =
  // Validation.buildDefaultValidatorFactory();

  @Default
  @Produces
  public Logger produceLog(InjectionPoint injectionPoint) {
    return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
  }

  //    @Default
  //    @Produces
  //    public Validator produceValidator() {
  //        return validatorFactory.getValidator();
  //    }

  //    @Produces
  //    @PersistenceContext(unitName = "pu-dss")
  //    private EntityManager em;

}
