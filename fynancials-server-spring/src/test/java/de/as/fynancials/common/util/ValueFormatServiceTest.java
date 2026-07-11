package de.as.fynancials.common.util;

import static java.util.Locale.US;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueFormatServiceTest {

  private ValueFormatService subject;
  private Locale locale;

  @BeforeEach
  void beforeEach() {
    locale = Locale.getDefault();
    Locale.setDefault(US);
    Clock clock = mock(Clock.class);
    when(clock.instant()).thenReturn(Instant.parse("2024-01-01T16:37:08Z"));
    when(clock.getZone()).thenReturn(ZoneId.of("Europe/Berlin"));
    subject = new ValueFormatServiceImpl(clock);
  }

  @AfterEach
  void afterEach() {
    Locale.setDefault(locale);
  }

  @Test
  void shouldReturnEmptyFormattedValue_whenInputIsNull() {
    FormattedValue result = subject.formatValue(null, null);
    assertThat(result.getFormattedValue()).isEmpty();
    assertThat(result.toString()).isEmpty();
  }

  @Test
  void shouldReturnUnchangedString_whenNoFunctionsPresent() {
    String input = "Hello World 123";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo(input);
    assertThat(result.toString()).isEqualTo(input);
  }

  @Test
  void shouldReplaceUuidFunction_withValidUuid() {
    String input = "id:#uuid()";
    FormattedValue result = subject.formatValue(input, null);
    String uuidStr = result.getFormattedValue().substring(3);
    assertThat(UUID.fromString(uuidStr)).isNotNull();
    assertThat(result.toString()).isEqualTo(result.getFormattedValue());
  }

  @Test
  void shouldReplaceRngFunction_withinBounds() {
    String input = "#rng(10,15)";
    FormattedValue result = subject.formatValue(input, null);
    int value = Integer.parseInt(result.getFormattedValue());
    assertThat(value).isBetween(10, 15);
    assertThat(result.toString()).isEqualTo("" + value);
  }

  @Test
  void shouldReplaceBase64Function() {
    String input = "#base64(hello)";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo("aGVsbG8=");
    assertThat(result.toString()).isEqualTo("aGVsbG8=");
  }

  @Test
  void shouldReplaceDateFunction_withDifferentArguments() {
    FormattedValue value = subject.formatValue("#date()", null);
    assertThat(value.getFormattedValue()).isEqualTo("2024-01-01");
    assertThat(value.toString()).isEqualTo("2024-01-01");

    value = subject.formatValue("#date(yyyy/MM/dd, 10)", null);
    assertThat(value.getFormattedValue()).isEqualTo("2023/12/22");
    assertThat(value.toString()).isEqualTo("2023/12/22");

    value = subject.formatValue("#date(dd.MM.yyyy, E, 1)", null);
    assertThat(value.getFormattedValue()).isEqualTo("31.12.2023, Sun");
    assertThat(value.toString()).isEqualTo("31.12.2023, Sun");
  }

  @Test
  void shouldHandleNestedMaskAndBase64Functions() {
    String input = "abc#mask(#base64(user:pw))def";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo("abcdXNlcjpwdw==def");
    assertThat(result.toString()).isEqualTo("abc*****def");
  }

  @Test
  void shouldReplaceIdFunction_whenIdIsProvided() {
    String input = "user_#id()";
    String id = "12345";
    FormattedValue result = subject.formatValue(input, id);
    assertThat(result.getFormattedValue()).isEqualTo("user_12345");
    assertThat(result.toString()).isEqualTo("user_12345");
  }

  @Test
  void shouldReplaceIdFunctionWithNullString_whenIdIsNull() {
    String input = "id:#id()";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo("id:null");
    assertThat(result.toString()).isEqualTo("id:null");
  }

  @Test
  void shouldCollectMaskedValues_andLeaveValueInStringUnchanged() {
    String input = "secret_password_is_#mask(secret123)";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo("secret_password_is_secret123");
    assertThat(result.toString()).isEqualTo("secret_password_is_*****");
  }

  @Test
  void shouldCollectMultipleMaskedValues() {
    String input = "#mask(admin) / #mask(password)";
    FormattedValue result = subject.formatValue(input, null);
    assertThat(result.getFormattedValue()).isEqualTo("admin / password");
    assertThat(result.toString()).isEqualTo("***** / *****");
  }

  @Test
  void shouldCombineIdAndMaskFunctions() {
    String input = "session_#id()_#mask(confidential)";
    String id = "abc";
    FormattedValue result = subject.formatValue(input, id);
    assertThat(result.getFormattedValue()).isEqualTo("session_abc_confidential");
    assertThat(result.toString()).isEqualTo("session_abc_*****");
  }

  @Test
  void shouldReturnZeroForIdCount_whenInputIsNull() {
    assertThat(subject.countIdTemplates(null)).isEqualTo(0);
  }

  @Test
  void shouldReturnZeroForIdCount_whenNoIdTemplatePresent() {
    assertThat(subject.countIdTemplates("Hello #uuid() and #mask(test)")).isEqualTo(0);
  }

  @Test
  void shouldCountSingleIdTemplate() {
    assertThat(subject.countIdTemplates("user_#id()_data")).isEqualTo(1);
  }

  @Test
  void shouldCountMultipleIdTemplates() {
    String input = "#id()_prefix_#id()_suffix_#id()";
    assertThat(subject.countIdTemplates(input)).isEqualTo(3);
  }
}
