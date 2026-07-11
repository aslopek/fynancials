package integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@Configuration
public class IntegrationTestConfig {

  @Bean
  public MockRestServiceServer mockServer(RestTemplate restTemplate) {
    return MockRestServiceServer.createServer(restTemplate);
  }
}
