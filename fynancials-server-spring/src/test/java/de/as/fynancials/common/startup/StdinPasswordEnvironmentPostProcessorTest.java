package de.as.fynancials.common.startup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

class StdinPasswordEnvironmentPostProcessorTest {

  private static final String MARKER = "FY_DB_FILE_PASSWORD_STDIN";
  private static final String PROPERTY = "FY_DB_FILE_PASSWORD";
  private static final String STDIN_SOURCE_NAME = "fynancialsStdinPassword";

  private Log log;
  private StdinPasswordHandover handover;
  private ConfigurableEnvironment environment;
  private MutablePropertySources sources;
  private SpringApplication application;
  private ArgumentCaptor<PropertySource<?>> contribution;
  private StdinPasswordEnvironmentPostProcessor subject;

  @BeforeEach
  void beforeEach() {
    log = mock(Log.class);
    handover = mock(StdinPasswordHandover.class);
    environment = mock(ConfigurableEnvironment.class);
    sources = mock(MutablePropertySources.class);
    application = mock(SpringApplication.class);
    contribution = ArgumentCaptor.captor();

    when(environment.getProperty(MARKER)).thenReturn("true");
    when(environment.getPropertySources()).thenReturn(sources);
    when(handover.read(log)).thenReturn(Optional.of("hunter2"));

    subject = new StdinPasswordEnvironmentPostProcessor(log, handover);
  }

  @Test
  void contributesTheStreamsContentAsThePasswordProperty() {
    subject.postProcessEnvironment(environment, application);

    verify(sources).addFirst(contribution.capture());
    assertThat(contribution.getValue())
        .extracting(PropertySource::getName, PropertySource::getSource)
        .containsExactly(STDIN_SOURCE_NAME, Map.of(PROPERTY, "hunter2"));
  }

  @Test
  void contributesItAtTheHighestPrecedenceAndLeavesItThere() {
    subject.postProcessEnvironment(environment, application);

    verify(sources).addFirst(any());
    verifyNoMoreInteractions(sources);
  }

  @Test
  void logsNothingOnASuccessfulContribution() {
    subject.postProcessEnvironment(environment, application);

    verifyNoInteractions(log);
  }

  @Test
  void contributesAnEmptyPasswordForAnEmptyStream() {
    when(handover.read(log)).thenReturn(Optional.of(""));

    subject.postProcessEnvironment(environment, application);

    verify(sources).addFirst(contribution.capture());
    assertThat(contribution.getValue())
        .extracting(PropertySource::getName, PropertySource::getSource)
        .containsExactly(STDIN_SOURCE_NAME, Map.of(PROPERTY, ""));
  }

  @Test
  void contributesNothingWhenNoHandoverCompleted() {
    when(handover.read(log)).thenReturn(Optional.empty());

    subject.postProcessEnvironment(environment, application);

    verifyNoInteractions(sources);
  }

  @Test
  void neverReadsStdinWithoutTheMarker() {
    when(environment.getProperty(MARKER)).thenReturn(null);

    subject.postProcessEnvironment(environment, application);

    verifyNoInteractions(handover, sources);
  }

  @Test
  void neverReadsStdinForABlankMarker() {
    when(environment.getProperty(MARKER)).thenReturn("");

    subject.postProcessEnvironment(environment, application);

    verifyNoInteractions(handover, sources);
  }
}
