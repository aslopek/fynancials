package de.as.traquity.notification.dividendannouncement.datasource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DividendAnnouncementDataSourceRepository extends JpaRepository<DividendAnnouncementDataSourceEntity, Long> {
}
