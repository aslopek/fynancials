package de.as.fynancials.common.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {

  private static final DateTimeFormatter[] DATE_FORMATS;

  static {
    DATE_FORMATS = new DateTimeFormatter[11];
    DATE_FORMATS[0] = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DATE_FORMATS[1] = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DATE_FORMATS[2] = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DATE_FORMATS[3] = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DATE_FORMATS[4] = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    DATE_FORMATS[5] = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DATE_FORMATS[6] = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    DATE_FORMATS[7] = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    DATE_FORMATS[8] = DateTimeFormatter.ofPattern("dd MMM yyyy");
    DATE_FORMATS[9] = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    DATE_FORMATS[10] = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
  }

  public LocalDate parseDate(String s) throws IllegalArgumentException {
    if (s == null) {
      return null;
    }

    for (DateTimeFormatter dateTimeFormatter : DATE_FORMATS) {
      try {
        return LocalDate.parse(s, dateTimeFormatter);
      } catch (DateTimeParseException e) {
      }
    }

    throw new IllegalArgumentException(s);
  }
}
