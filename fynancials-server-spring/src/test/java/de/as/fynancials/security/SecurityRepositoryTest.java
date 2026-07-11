package de.as.fynancials.security;

import static integration.SecurityIds.AAPL;
import static integration.SecurityIds.MSFT;
import static org.assertj.core.api.Assertions.assertThat;

import integration.IntegrationTest;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class SecurityRepositoryTest {

  @Autowired
  private SecurityRepository repository;

  @Test
  void findSecurityIdsBySymbols_singleExistingSymbol_returnsOwnerId() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("MSFT"));
    assertThat(ids).containsExactly(MSFT);
  }

  @Test
  void findSecurityIdsBySymbols_multipleSymbolsOfSameSecurity_returnsSingleId() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("MSFT", "MSF.DE"));
    assertThat(ids).containsExactly(MSFT);
  }

  @Test
  void findSecurityIdsBySymbols_symbolsOfDifferentSecurities_returnsAllOwnerIds() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("MSFT", "AAPL"));
    assertThat(ids).containsExactlyInAnyOrder(MSFT, AAPL);
  }

  @Test
  void findSecurityIdsBySymbols_unknownSymbol_returnsEmpty() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("DOES_NOT_EXIST"));
    assertThat(ids).isEmpty();
  }

  @Test
  void findSecurityIdsBySymbols_mixedKnownAndUnknownSymbols_returnsOnlyKnownOwner() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("MSFT", "DOES_NOT_EXIST"));
    assertThat(ids).containsExactly(MSFT);
  }

  @Test
  void findSecurityIdsBySymbols_differentCase_returnsEmpty() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of("msft"));
    assertThat(ids).isEmpty();
  }

  @Test
  void findSecurityIdsBySymbols_emptyInput_returnsEmpty() {
    Set<Long> ids = repository.findSecurityIdsBySymbols(Set.of());
    assertThat(ids).isEmpty();
  }
}
