package de.as.fynancials.notification.dividendannouncement.config;

import static org.assertj.core.api.Assertions.assertThat;

import integration.IntegrationTest;
import integration.SecurityIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class DividendAnnouncementConfigRepositoryTest {

  @Autowired
  private DividendAnnouncementConfigRepository repository;

  @Test
  void deleteBySecurityId_noConfigExists_nothingChanges() {
    long count = repository.count();
    long deletedRows = repository.deleteAllBySecurityId(SecurityIds.GOOGL);
    assertThat(deletedRows).isZero();
    assertThat(repository.count()).isEqualTo(count);
  }

  @Test
  void deleteBySecurityId_securityIdExists_configDeleted() {
    long count = repository.count();
    long deletedRows = repository.deleteAllBySecurityId(SecurityIds.MSFT);
    assertThat(deletedRows).isOne();
    assertThat(repository.count()).isEqualTo(count - deletedRows);
  }
}
