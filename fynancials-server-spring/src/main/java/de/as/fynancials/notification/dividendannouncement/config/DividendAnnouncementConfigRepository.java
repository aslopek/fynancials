package de.as.fynancials.notification.dividendannouncement.config;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface DividendAnnouncementConfigRepository extends JpaRepository<DividendAnnouncementConfigEntity, Long> {

  @Modifying
  @Transactional
  @Query("""
      DELETE FROM DividendAnnouncementConfigEntity e
      WHERE e.securityId = :securityId
      """)
  int deleteAllBySecurityId(long securityId);

  boolean existsBySecurityId(long securityId);

  List<DividendAnnouncementConfigEntity> findAllByOrderBySecurityIdAsc();

  Optional<DividendAnnouncementConfigEntity> findBySecurityId(long securityId);
}
