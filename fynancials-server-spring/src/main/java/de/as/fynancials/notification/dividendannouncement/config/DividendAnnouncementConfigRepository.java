package de.as.fynancials.notification.dividendannouncement.config;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DividendAnnouncementConfigRepository extends JpaRepository<DividendAnnouncementConfigEntity, Long> {

  boolean existsBySecurityId(long securityId);

  List<DividendAnnouncementConfigEntity> findAllByOrderBySecurityIdAsc();

  Optional<DividendAnnouncementConfigEntity> findBySecurityId(long securityId);
}
