import {Component, computed, input, InputSignal, Signal} from "@angular/core";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {FyCurrencyPipe, FyDatePipe, SecurityNamePipe, TransactionTypeDisplayIconPipe, TransactionTypeDisplayNamePipe} from "../../../../common";
import {NgClass} from "@angular/common";
import {Store} from "@ngrx/store";
import {AppState} from "../../../../store/app.state";
import {selectedDepotCurrency} from "../../../../store/depot/depot.selector";
import {MatIcon} from "@angular/material/icon";
import {MatTooltip} from "@angular/material/tooltip";
import {TransactionWithCashFlow} from "../../store/computed/grouped-transactions";
import {SecurityLogoComponent} from "../../../../common/components/security-logo/security-logo.component";

@Component({
  selector: "app-transaction-group",
  imports: [
    MatCard,
    MatCardHeader,
    MatCardTitle,
    FyDatePipe,
    MatCardContent,
    NgClass,
    TransactionTypeDisplayIconPipe,
    SecurityNamePipe,
    FyCurrencyPipe,
    MatIcon,
    MatTooltip,
    TransactionTypeDisplayNamePipe,
    SecurityLogoComponent
  ],
  templateUrl: "./transaction-group.component.html",
  styleUrl: "./transaction-group.component.scss",
})
export class TransactionGroupComponent {

  readonly transactions: InputSignal<TransactionWithCashFlow[]> = input.required<TransactionWithCashFlow[]>();
  protected readonly currency: Signal<string>;
  protected readonly groupedTransactions: Signal<TransactionWithCashFlow[]>;

  constructor(globalStore: Store<AppState>) {
    this.currency = globalStore.selectSignal(selectedDepotCurrency);
    this.groupedTransactions = computed<TransactionWithCashFlow[]>((): TransactionWithCashFlow[] => {
      const result: TransactionWithCashFlow[] = [];
      const transactionIndices: { [transactionKey: string]: number } = {};
      let transactionKey: string;

      for (const transaction of this.transactions()) {
        transactionKey = `${transaction.securityId}/${transaction.transactionType}`;
        if (transactionIndices[transactionKey] === undefined) {
          transactionIndices[transactionKey] = result.length;
          result.push(transaction);
        } else {
          result[transactionIndices[transactionKey]].cashFlow += transaction.cashFlow;
        }
      }
      return result;
    });
  }
}
