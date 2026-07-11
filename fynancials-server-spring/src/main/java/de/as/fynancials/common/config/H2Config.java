package de.as.fynancials.common.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class H2Config {

  @Value("${server.h2.console.enabled:false}")
  private boolean enabled;

  @Value("${server.h2.console.port:0}")
  private int port;

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Server h2Console() throws java.sql.SQLException {
    if (!enabled) {
      return null;
    }
    return Server.createWebServer(
        "-web",
        "-webPort", String.valueOf(port)
    );
  }

  @Bean
  public String h2ConsolePath() {
    if (!enabled) {
      return "";
    }
    return String.format("http://localhost:%d", this.port);
  }
}
