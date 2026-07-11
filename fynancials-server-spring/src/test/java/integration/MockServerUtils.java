package integration;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.io.InputStream;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;

@UtilityClass
public final class MockServerUtils {

  public static ResponseActions respondWithFixture(MockRestServiceServer mockServer, String url, String pathToFixture) {
    byte[] responseBody;
    try (InputStream inputStream = MockServerUtils.class.getClassLoader().getResourceAsStream(pathToFixture)) {
      if (inputStream == null) {
        throw new IOException();
      }
      responseBody = inputStream.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Content-Type", "application/json");
    ResponseActions responseActions = mockServer.expect(ExpectedCount.once(), requestTo(url)).andExpect(method(HttpMethod.GET));
    responseActions.andRespond(withStatus(HttpStatus.OK).body(responseBody).headers(responseHeaders));
    return responseActions;
  }

  public static void respondWithStatus(MockRestServiceServer mockRestServiceServer, String url, HttpStatus status) {
    mockRestServiceServer.expect(ExpectedCount.once(), requestTo(url)).andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(status));
  }
}
