package integration.api;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.experimental.UtilityClass;
import org.springframework.test.web.client.RequestMatcher;

@UtilityClass
public class MockRestRequestMatchers {

  public static RequestMatcher withoutHeader(String name) {
    return request -> {
      assertThat(request.getHeaders().containsHeader(name)).isFalse();
    };
  }
}
