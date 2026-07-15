package de.as.fynancials.configuration.securitygroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SecurityGroupRepository extends JpaRepository<SecurityGroupEntity, Long> {

}
