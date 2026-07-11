package de.as.fynancials.configuration;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ServerConfigurationRepository extends JpaRepository<ServerConfigurationEntity, Long> {

  Optional<ServerConfigurationEntity> findByConfigKey(String configKey);
}
