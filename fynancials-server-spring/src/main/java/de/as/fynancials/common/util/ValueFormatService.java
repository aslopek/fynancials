package de.as.fynancials.common.util;

public interface ValueFormatService {

  FormattedValue formatValue(String value, String id);

  int countIdTemplates(String value);
}
