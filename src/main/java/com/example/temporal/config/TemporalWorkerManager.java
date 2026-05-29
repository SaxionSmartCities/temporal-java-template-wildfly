package com.example.temporal.config;

import com.example.temporal.workflows.crawler.CrawlerActivities;
import com.example.temporal.workflows.crawler.CrawlerWorker;
import com.example.temporal.workflows.crawler.CrawlerWorkflowImpl;
import com.example.temporal.workflows.http.HttpActivities;
import com.example.temporal.workflows.http.HttpWorker;
import com.example.temporal.workflows.http.HttpWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Container-managed Temporal worker lifecycle manager. */
@Startup
@Singleton
public class TemporalWorkerManager {

  private static final Logger logger = LoggerFactory.getLogger(TemporalWorkerManager.class);

  @Inject private Instance<WorkflowClient> workflowClientInstance;

  @Inject private Instance<HttpActivities> httpActivitiesInstance;

  @Inject private Instance<CrawlerActivities> crawlerActivitiesInstance;

  private WorkerFactory workerFactory;

  @PostConstruct
  public void init() {
    logger.info("Initializing Temporal Workers...");

    // Force loading of classes that might be needed during shutdown to avoid NoClassDefFoundError
    // in Wildfly when the classloader is partially closed.
    try {
      Class.forName("io.temporal.api.workflowservice.v1.ShutdownWorkerRequest$Builder");
    } catch (ClassNotFoundException e) {
      logger.warn("Could not pre-load ShutdownWorkerRequest$Builder class", e);
    }

    WorkflowClient workflowClient = workflowClientInstance.get();
    HttpActivities httpActivities = httpActivitiesInstance.get();
    CrawlerActivities crawlerActivities = crawlerActivitiesInstance.get();

    workerFactory = WorkerFactory.newInstance(workflowClient);

    // HTTP Worker
    Worker httpWorker = workerFactory.newWorker(HttpWorker.TASK_QUEUE);
    httpWorker.registerWorkflowImplementationTypes(HttpWorkflowImpl.class);
    httpWorker.registerActivitiesImplementations(httpActivities);

    // Crawler Worker
    WorkerOptions crawlerWorkerOptions =
        WorkerOptions.newBuilder().setMaxConcurrentActivityExecutionSize(16).build();
    Worker crawlerWorker = workerFactory.newWorker(CrawlerWorker.TASK_QUEUE, crawlerWorkerOptions);
    crawlerWorker.registerWorkflowImplementationTypes(CrawlerWorkflowImpl.class);
    crawlerWorker.registerActivitiesImplementations(crawlerActivities);

    workerFactory.start();
    logger.info("Temporal Workers started.");
  }

  @PreDestroy
  public void shutdown() {
    logger.info("Shutting down Temporal Workers...");
    if (workerFactory != null) {
      workerFactory.shutdown();
    }
  }
}
