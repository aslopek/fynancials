package integration;

import java.math.MathContext;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Arithmetic {

  /**
   * Mirrors the {@code MathContext} bean the application runs with, so that tests measure the precision and rounding mode that ships.
   */
  public static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.HALF_UP);
}
