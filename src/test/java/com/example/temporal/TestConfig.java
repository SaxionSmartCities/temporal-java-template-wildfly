package com.example.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.testing.TestWorkflowEnvironment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

/**
 * Test configuration for Temporal workflow tests.
 *
 * <p>This configuration provides a TestWorkflowEnvironment for integration testing with
 * time-skipping capabilities.
 */
@ApplicationScoped
public class TestConfig {

  /**
   * Creates a TestWorkflowEnvironment bean for testing.
   *
   * <p>TestWorkflowEnvironment allows for time-skipping and deterministic testing of workflows.
   *
   * @return A new TestWorkflowEnvironment instance
   */
  @Produces
  @ApplicationScoped
  public TestWorkflowEnvironment testWorkflowEnvironment() {
    return TestWorkflowEnvironment.newInstance();
  }

  /**
   * Disposes the TestWorkflowEnvironment.
   *
   * @param testEnv The environment to dispose
   */
  public void disposeTestWorkflowEnvironment(@Disposes TestWorkflowEnvironment testEnv) {
    if (testEnv != null) {
      testEnv.close();
    }
  }

  /**
   * Creates a WorkflowClient bean from the test environment.
   *
   * @param testEnv The test workflow environment
   * @return A WorkflowClient for test execution
   */
  @Produces
  @ApplicationScoped
  public WorkflowClient testWorkflowClient(TestWorkflowEnvironment testEnv) {
    return testEnv.getWorkflowClient();
  }
}
