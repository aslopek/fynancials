package integration;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;
import org.assertj.core.data.Offset;

@UtilityClass
public class Accuracy {

  public static final Offset<BigDecimal> ACCURACY_ONE_THOUSANDTH = Offset.strictOffset(new BigDecimal("0.001"));
}
