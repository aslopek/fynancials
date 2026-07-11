package de.as.fynancials.depot.transaction;

import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

  boolean existsByIdAndDepotId(Long id, Long depotId);

  Optional<TransactionEntity> findByIdAndDepotId(Long id, Long depotId);

  List<TransactionEntity> findAllByDepotIdInAndTransactionTypeInOrderByDateAscTimeAsc(Set<Long> depotIds,
                                                                                      Set<TransactionTypeDto> types);

  List<TransactionEntity> findAllByDepotIdInAndSecurityIdInAndTransactionTypeInOrderByDateAscTimeAsc(Set<Long> depotIds,
                                                                                                     Set<Long> securityIds,
                                                                                                     Set<TransactionTypeDto> types);

  List<TransactionEntity> findAllByDepotIdAndTransactionTypeAndDate(Long depotId, TransactionTypeDto type,
                                                                    LocalDate date);

  Page<TransactionEntity> findAll(Specification<TransactionEntity> specification, Pageable pageRequest);

  List<TransactionEntity> findAllBySecurityIdAndDateLessThan(Long securityId, LocalDate date);

  @Transactional
  void deleteByIdAndDepotId(Long id, Long depotId);
}
