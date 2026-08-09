package de.as.fynancials.common.startup;

import static lombok.AccessLevel.PACKAGE;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Contributes the database password handed over on stdin as the {@code FY_DB_FILE_PASSWORD} property,
 * ahead of the system environment, whenever the packaged Electron spawn set the {@code FY_DB_FILE_PASSWORD_STDIN}
 * marker. Without that marker - every standalone dev start - this is a no-op and {@code System.in} is never touched.
 * </p>
 *
 * <p>
 * Registered via {@code META-INF/spring.factories}, not {@code META-INF/spring/*.imports}: the latter mechanism
 * exists only for auto-configurations, not for {@link EnvironmentPostProcessor}s.
 * </p>
 */
@RequiredArgsConstructor(access = PACKAGE)
public class StdinPasswordEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String MARKER = "FY_DB_FILE_PASSWORD_STDIN";
  private static final String PROPERTY = "FY_DB_FILE_PASSWORD";
  private static final String PROPERTY_SOURCE_NAME = "fynancialsStdinPassword";
  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final int LIMIT_IN_BYTES = 4096;

  /**
   * static because the handover is one-shot per JVM: System.in reaches EOF exactly once, so a second post-processor
   * instance holding its own handover would read that EOF and contribute an empty password over a good one
   */
  private static final StdinPasswordHandover SYSTEM_IN = new StdinPasswordHandover(System.in, TIMEOUT, LIMIT_IN_BYTES);

  private final Log log;
  private final StdinPasswordHandover handover;

  public StdinPasswordEnvironmentPostProcessor(DeferredLogFactory logFactory) {
    this(logFactory.getLog(StdinPasswordEnvironmentPostProcessor.class), SYSTEM_IN);
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    if (!StringUtils.hasText(environment.getProperty(MARKER))) {
      return;
    }
    handover.read(log).ifPresent(password -> environment.getPropertySources()
        .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of(PROPERTY, password))));
  }
}
