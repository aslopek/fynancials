package de.as.fynancials.common.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
    requestFactory.setReadTimeout(READ_TIMEOUT);
    return new RestTemplate(requestFactory);
  }
}
