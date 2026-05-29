package com.example.temporal.api;

import com.example.temporal.workflows.http.HttpWorker;
import com.example.temporal.workflows.http.HttpWorkflow;
import com.example.temporal.workflows.http.HttpWorkflowInput;
import com.example.temporal.workflows.http.HttpWorkflowOutput;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

/** JAX-RS resource for HTTP calls via Temporal workflows. */
@Path("/https-calls")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class HttpsCallsResource {

  @Inject private WorkflowClient workflowClient;

  /**
   * Starts an HTTP workflow to fetch content from the specified URL.
   *
   * @param url The URL to fetch
   * @return Response containing the workflow output
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response startHttpWorkflow(@FormParam("url") String url) {
    if (url == null || url.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("URL parameter is required")
          .build();
    }

    String workflowId = "http-workflow-" + UUID.randomUUID();

    HttpWorkflow workflow =
        workflowClient.newWorkflowStub(
            HttpWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(HttpWorker.TASK_QUEUE)
                .build());

    HttpWorkflowInput input = new HttpWorkflowInput(url);
    HttpWorkflowOutput output = workflow.run(input);

    return Response.ok(output).build();
  }
}
