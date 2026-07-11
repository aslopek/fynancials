package de.as.fynancials.depot.performance.income;

import de.as.fynancials.common.error.BadRequestException;
import de.as.fynancials.common.error.NotFoundException;
import de.as.fynancials.configuration.securitygroup.SecurityGroup;
import de.as.fynancials.configuration.securitygroup.SecurityGroupService;
import de.as.fynancials.depot.DepotService;
import de.as.fynancials.depot.performance.api.model.IncomeTypeDto;
import de.as.fynancials.depot.performance.model.Performance;
import de.as.fynancials.depot.transaction.Transaction;
import de.as.fynancials.depot.transaction.TransactionService;
import de.as.fynancials.depot.transaction.api.model.TransactionTypeDto;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class IncomeServiceImpl implements IncomeService {

  private final MathContext mathContext;
  private final TransactionService transactionService;
  private final SecurityGroupService securityGroupService;
  private final DepotService depotService;

  @Override
  public List<Performance> getIncome(Set<Long> depotIds, Set<Long> securityIds, Set<IncomeTypeDto> incomeTypes)
      throws BadRequestException {
    if (depotIds == null || depotIds.isEmpty() || securityIds == null || securityIds.isEmpty() || incomeTypes == null
        || incomeTypes.isEmpty()) {
      throw new BadRequestException();
    }
    try {
      boolean depotsHaveSameCurrency = depotService.depotsHaveSameCurrency(depotIds);
      if (!depotsHaveSameCurrency) {
        throw new BadRequestException();
      }
    } catch (NotFoundException e) {
      throw new BadRequestException();
    }

    Set<TransactionTypeDto> transactionTypes = new HashSet<>();
    if (incomeTypes.contains(IncomeTypeDto.DIVIDEND)) {
      transactionTypes.add(TransactionTypeDto.DIVIDEND);
      transactionTypes.add(TransactionTypeDto.SPECIAL_DIVIDEND);
    }
    if (incomeTypes.contains(IncomeTypeDto.SELL)) {
      transactionTypes.add(TransactionTypeDto.SELL);
    }
    if (incomeTypes.contains(IncomeTypeDto.OTHER)) {
      transactionTypes.add(TransactionTypeDto.TAX);
    }

    List<Performance> income = new ArrayList<>(securityIds.size());
    Map<Long, SecurityGroup> securityGroups = securityGroupService.getSecurityGroupsBySecurityId();
    Map<Long, Performance> performanceBySecurityGroup = new HashMap<>(securityGroups.size());
    Performance performance;
    Performance bySecurityGroup;
    for (Long securityId : securityIds) {
      performance = getPerformanceBySecurity(depotIds, securityId, transactionTypes);

      if (securityGroups.containsKey(securityId)) {
        bySecurityGroup = performanceBySecurityGroup.get(securityGroups.get(securityId).getId());
        if (bySecurityGroup == null) {
          performanceBySecurityGroup.put(securityGroups.get(securityId).getId(), performance);
        } else {
          bySecurityGroup.add(performance, mathContext);
          bySecurityGroup.getSecurityIds().add(securityId);
        }
      } else {
        income.add(performance);
      }
    }

    income.addAll(performanceBySecurityGroup.values());
    Collections.sort(income);
    income = income.stream().filter(e -> e.getAbsoluteValueGross().compareTo(BigDecimal.ZERO) != 0
        && e.getAbsoluteValueNet().compareTo(BigDecimal.ZERO) != 0).toList();
    return income;
  }

  private Performance getPerformanceBySecurity(Set<Long> depotIds, Long securityId,
                                               Set<TransactionTypeDto> transactionTypes) {
    Performance performance = new Performance();
    performance.getSecurityIds().add(securityId);
    List<Transaction> transactions = transactionService.getTransactions(depotIds, Set.of(securityId), transactionTypes);
    performance.getTransactions().addAll(transactions);
    BigDecimal grossValue, netValue;

    for (Transaction transaction : transactions) {
      grossValue = transaction.getGrossValue();
      netValue = transaction.getNetValue(mathContext);
      if (TransactionTypeDto.TAX.equals(transaction.getTransactionType())) {
        grossValue = grossValue.negate();
        netValue = netValue.negate();
      }
      performance.setAbsoluteValueGross(performance.getAbsoluteValueGross().add(grossValue, mathContext));
      performance.setAbsoluteValueNet(performance.getAbsoluteValueNet().add(netValue, mathContext));
    }

    return performance;
  }
}
