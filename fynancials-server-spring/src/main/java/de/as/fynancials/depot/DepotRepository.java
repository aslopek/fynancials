package de.as.fynancials.depot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface DepotRepository extends JpaRepository<DepotEntity, Long> {

}
