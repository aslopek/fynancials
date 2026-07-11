package de.as.fynancials.configuration.securitygroup;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityGroupRepository extends JpaRepository<SecurityGroupEntity, Long> {

  @Transactional
  void deleteById(Long id);

  boolean existsByName(String name);

  Optional<SecurityGroupEntity> findByName(String name);
}
