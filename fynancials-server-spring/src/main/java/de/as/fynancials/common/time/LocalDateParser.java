package de.as.fynancials.common.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDateParser implements Function<String, LocalDate> {

  private record DateFormat(String dateFormat, Pattern pattern, Locale locale) {}

  private static final List<DateFormat> DATE_FORMATS =
      List.of(new DateFormat("MM/dd/yyyy", Pattern.compile("[0-9]{2}/[0-9]{2}/[0-9]{4}"), Locale.US));

  @Override
  public LocalDate apply(String value) {
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ignored) {
    }


    for (DateFormat format : DATE_FORMATS) {
      try {
        return parseString(value, format);
      } catch (DateTimeParseException ignored) {
      }
    }

    log.error("Could not parse date {}", value);
    throw new IllegalArgumentException();
  }

  private LocalDate parseString(String value, DateFormat format) throws DateTimeParseException {
    if (format == null) {
      return LocalDate.parse(value);
    }

    DateTimeFormatter dateTimeFormatter;
    if (format.locale == null) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(format.dateFormat());
    } else {
      dateTimeFormatter = DateTimeFormatter.ofPattern(format.dateFormat, format.locale);
    }

    return LocalDate.parse(value, dateTimeFormatter);
  }
}
