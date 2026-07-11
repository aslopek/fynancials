package de.as.fynancials.notification.dividendannouncement;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DividendAnnouncementRepository extends JpaRepository<DividendAnnouncementEntity, Long> {

  @Transactional
  void deleteAllByPayDateBefore(LocalDate payDate);

  List<DividendAnnouncementEntity> findAllByOrderByPayDateAsc();

  List<DividendAnnouncementEntity> findAllByIsNewOrderByPayDateAsc(boolean isNew);

  boolean existsBySecurityIdAndPayDateAndAmountPerShare(long securityId, LocalDate payDate, BigDecimal amountPerShare);
}
