package de.as.fynancials.common.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SpringSecurityConfig {

  @Value("${cors.allowed-origins}")
  private String allowedOrigins;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    if (allowedOrigins != null && !allowedOrigins.isBlank()) {
      corsConfiguration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    }
    corsConfiguration.setAllowedMethods(List.of("*"));
    corsConfiguration.setAllowedHeaders(List.of("*"));
    if (!"*".equals(allowedOrigins)) {
      corsConfiguration.setAllowCredentials(true);
    }
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity.csrf(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)).build();
  }
}
