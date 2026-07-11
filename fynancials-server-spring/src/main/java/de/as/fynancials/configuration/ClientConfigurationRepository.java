package de.as.fynancials.configuration;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ClientConfigurationRepository extends JpaRepository<ClientConfigurationEntity, Long> {

  @Transactional
  void deleteAllByClientId(String clientId);

  @Transactional
  void deleteByClientIdAndConfigKey(String clientId, String configKey);

  List<ClientConfigurationEntity> findAllByClientId(String clientId);

  List<ClientConfigurationEntity> findAllByClientIdAndConfigKeyStartsWith(String configKey, String prefix);

  Optional<ClientConfigurationEntity> findByClientIdAndConfigKey(String clientId, String configKey);
}
