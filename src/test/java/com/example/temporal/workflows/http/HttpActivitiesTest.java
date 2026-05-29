package com.example.temporal.workflows.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for HttpActivitiesImpl.
 *
 * <p>These tests use Mockito to mock Jakarta REST Client and verify activity behavior.
 */
@ExtendWith(MockitoExtension.class)
class HttpActivitiesTest {

  @Mock private Client client;
  @Mock private WebTarget webTarget;
  @Mock private Invocation.Builder builder;
  @Mock private Response response;

  private HttpActivitiesImpl activities;

  @BeforeEach
  void setUp() {
    activities = new HttpActivitiesImpl(client);
  }

  @Test
  void testHttpGet_Success() {
    // Arrange
    String url = "https://example.com";
    String responseBody = "<html><body>Hello World</body></html>";

    when(client.target(url)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    when(response.readEntity(String.class)).thenReturn(responseBody);
    when(response.getStatus()).thenReturn(200);

    // Act
    HttpGetActivityInput input = new HttpGetActivityInput(url);
    HttpGetActivityOutput output = activities.httpGet(input);

    // Assert
    assertNotNull(output);
    assertEquals(responseBody, output.responseText());
    assertEquals(200, output.statusCode());
  }

  @Test
  void testHttpGet_EmptyResponse() {
    // Arrange
    String url = "https://example.com";

    when(client.target(url)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    when(response.readEntity(String.class)).thenReturn("");
    when(response.getStatus()).thenReturn(200);

    // Act
    HttpGetActivityInput input = new HttpGetActivityInput(url);
    HttpGetActivityOutput output = activities.httpGet(input);

    // Assert
    assertNotNull(output);
    assertEquals("", output.responseText());
    assertEquals(200, output.statusCode());
  }

  @Test
  void testHttpGet_NonOkStatus() {
    // Arrange
    String url = "https://example.com";
    String responseBody = "Not Found";

    when(client.target(url)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    when(response.readEntity(String.class)).thenReturn(responseBody);
    when(response.getStatus()).thenReturn(404);

    // Act
    HttpGetActivityInput input = new HttpGetActivityInput(url);
    HttpGetActivityOutput output = activities.httpGet(input);

    // Assert
    assertNotNull(output);
    assertEquals(responseBody, output.responseText());
    assertEquals(404, output.statusCode());
  }

  @Test
  void testHttpGet_NetworkError() {
    // Arrange
    String url = "https://invalid-url.com";
    when(client.target(url)).thenReturn(webTarget);
    when(webTarget.request()).thenReturn(builder);
    when(builder.get()).thenThrow(new WebApplicationException("Connection refused"));

    // Act & Assert
    HttpGetActivityInput input = new HttpGetActivityInput(url);
    assertThrows(WebApplicationException.class, () -> activities.httpGet(input));
  }
}
