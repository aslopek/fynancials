package de.as.fynancials.depot.transaction;

import de.as.fynancials.depot.transaction.api.model.SortOrderDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
class TransactionSpecification implements Specification<TransactionEntity> {

  private final TransactionPageRequest request;

  @Override
  public Predicate toPredicate(Root<TransactionEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    List<Predicate> predicates = new ArrayList<>();

    if (request.getDepotIds() != null && !request.getDepotIds().isEmpty()) {
      predicates.add(root.get("depotId").in(request.getDepotIds()));
    }

    if (request.getTransactionTypes() != null && !request.getTransactionTypes().isEmpty()) {
      predicates.add(root.get("transactionType").in(request.getTransactionTypes()));
    }

    if (request.getMinDate() != null) {
      predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), request.getMinDate()));
    }

    if (request.getMaxDate() != null) {
      predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), request.getMaxDate()));
    }

    if (request.getSecurityIds() != null && !request.getSecurityIds().isEmpty()) {
      predicates.add(root.get("securityId").in(request.getSecurityIds()));
    }

    if (request.getOrderByTime() == null || request.getOrderByTime() == SortOrderDto.ASC) {
      query.orderBy(criteriaBuilder.asc(root.get("date")), criteriaBuilder.asc(root.get("time")),
          criteriaBuilder.asc(root.get("id")));
    } else {
      query.orderBy(criteriaBuilder.desc(root.get("date")), criteriaBuilder.desc(root.get("time")),
          criteriaBuilder.desc(root.get("id")));
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }
}
