package integration.sql;

import de.as.fynancials.depot.transaction.Transaction;
import de.as.fynancials.price.security.historical.HistoricalSecurityPrice;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestDataQuery {

  private static final String DB_CONNECTION = "jdbc:h2:mem:fynancials";
  private static final String DB_USER = "test-user";
  private static final String DB_PASSWORD = "test-password";

  static {
    try {
      DriverManager.registerDriver(new org.h2.Driver());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static long getDepotCount() {
    return getCount("SELECT COUNT(*) FROM DEPOT");
  }

  public static long getTransactionCount() {
    return getCount("SELECT COUNT(*) FROM TRANSACTION");
  }

  public static long getTransactionCountByDepotId(long depotId) {
    return getCount("SELECT COUNT(*) FROM TRANSACTION WHERE DEPOT_ID = " + depotId);
  }

  public static long getDividendAnnouncementCountBySecurityId(long securityId) {
    return getCount(String.format("SELECT COUNT(*) FROM DIVIDEND_ANNOUNCEMENT WHERE SECURITY_ID = %d", securityId));
  }

  public static long getDividendAnnouncementCountByDataSourceId(long dataSourceId) {
    return getCount(
        String.format("SELECT COUNT(*) FROM DIVIDEND_ANNOUNCEMENT WHERE DATA_SOURCE_ID = %d", dataSourceId));
  }

  public static long getDividendAnnouncementConfigCountBySecurityId(long securityId) {
    return getCount(
        String.format("SELECT COUNT(*) FROM DIVIDEND_ANNOUNCEMENT_CONFIG WHERE SECURITY_ID = %d", securityId));
  }

  public static long getDividendAnnouncementConfigCountByDataSourceId(long dataSourceId) {
    return getCount(
        String.format("SELECT COUNT(*) FROM DIVIDEND_ANNOUNCEMENT_CONFIG WHERE DATA_SOURCE_ID = %d", dataSourceId));
  }

  public static long getDividendAnnouncementDataSourceHeadersByDataSourceId(long dataSourceId) {
    return getCount(
        String.format("SELECT COUNT (*) FROM DIVIDEND_ANNOUNCEMENT_DATA_SOURCE_REQUEST_HEADER WHERE DATA_SOURCE_ID = %d",
            dataSourceId));
  }

  public static long getDividendAnnouncementDataSourceCurrencyMappingsByDataSourceId(long dataSourceId) {
    return getCount(String.format(
        "SELECT COUNT (*) FROM DIVIDEND_ANNOUNCEMENT_DATA_SOURCE_CURRENCY_MAPPING WHERE DATA_SOURCE_ID = %d",
        dataSourceId));
  }

  public static Transaction getTransaction(long transactionId) {
    try {
      Connection connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT * FROM TRANSACTION WHERE ID = " + transactionId);
      result.next();
      Transaction transaction = new Transaction();
      transaction.setId(transactionId);
      transaction.setVersion(result.getLong("VERSION"));
      transaction.setDate(result.getDate("DATE").toLocalDate());
      Time time = result.getTime("TIME");
      if (time != null) {
        transaction.setTime(time.toLocalTime());
      }
      transaction.setDepotId(result.getLong("DEPOT_ID"));
      transaction.setSecurityId(result.getLong("SECURITY_ID"));
      transaction.setSecurityCountOriginal(result.getBigDecimal("SECURITY_COUNT_ORIGINAL"));
      transaction.setSecurityCountSplitAdjusted(result.getBigDecimal("SECURITY_COUNT_SPLIT_ADJUSTED"));
      transaction.setGrossValue(result.getBigDecimal("GROSS_VALUE"));
      transaction.setTax(result.getBigDecimal("TAX"));
      transaction.setFee(result.getBigDecimal("FEE"));
      return transaction;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static long getHistoricalSecurityPriceCountBySecurityId(long securityId) {
    return getCount("SELECT COUNT(*) FROM HISTORICAL_SECURITY_PRICE WHERE SECURITY_ID = " + securityId);
  }

  public static HistoricalSecurityPrice getHistoricalSecurityPrice(long securityId, LocalDate date) {
    try {
      Connection connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery(
          "SELECT * FROM HISTORICAL_SECURITY_PRICE WHERE SECURITY_ID = " + securityId + " AND DATE = '"
              + date.toString() + "'");
      result.next();
      HistoricalSecurityPrice price = new HistoricalSecurityPrice();
      price.setSecurityId(securityId);
      price.setPrice(result.getBigDecimal("PRICE"));
      price.setCurrency(result.getString("CURRENCY"));
      price.setDate(date);
      return price;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static long getCount(String query) {
    try {
      Connection connection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery(query);
      result.next();
      return result.getLong(1);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
