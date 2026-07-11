import {Component, computed, inject, Signal, signal, WritableSignal} from "@angular/core";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatDialogRef} from "@angular/material/dialog";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {FieldTree, form, FormField, pattern, required, SchemaPathTree} from "@angular/forms/signals";
import {Store} from "@ngrx/store";
import {firstValueFrom} from "rxjs";
import {FyCurrencyPipe, TitleToolbarComponent, TransactionTypeDisplayIconPipe, TransactionTypeDisplayNamePipe} from "../../../common";
import {FyNetValuePipe} from "../../../common/pipe/fy-net-value.pipe";
import {TransactionApi, TransactionCreate, TransactionType} from "../../../gen/api/depot-transaction";
import {AppState} from "../../../store/app.state";
import {DepotActions} from "../../../store/depot/depot.actions";
import {selectedDepotCurrency, selectedDepotIds} from "../../../store/depot/depot.selector";
import {ReadableTransactionPageStore, transactionPageStore} from "../transaction-store/transaction-page.store";

type TransactionFormModel = {
  transactionType: TransactionType | null;
  isSpecialDividend: boolean;
  securityName: string;
  date: Date;
  time: string;
  securityCountOriginal: string;
  securityCountSplitAdjusted: string;
  grossValue: string;
  tax: string;
  fee: string;
};

type NetValueInput = {
  transactionType: TransactionType;
  grossValue: string;
  tax: string;
  fee: string;
};

const countRegex: RegExp = /^\d+([.,]\d+)?$/;
const monetaryRegex: RegExp = /^(0[.,]\d{0,2}|[1-9]\d*([.,]\d+)?)$/;
const timeRegex: RegExp = /^([0-1][0-9]|2[0-3]):[0-5][0-9]$/;

@Component({
  selector: "app-transaction-create",
  imports: [
    TitleToolbarComponent,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatButtonModule,
    TransactionTypeDisplayNamePipe,
    MatSelectModule,
    MatIconModule,
    TransactionTypeDisplayIconPipe,
    MatCheckboxModule,
    FyNetValuePipe,
    FyCurrencyPipe,
    FormField,
  ],
  templateUrl: "./transaction-create.component.html",
  styleUrl: "./transaction-create.component.scss",
})
export class TransactionCreateComponent {
  protected readonly TransactionType = TransactionType;
  protected readonly allTransactionTypes: TransactionType[] = Object.values(TransactionType)
    .filter((value: string): boolean => value !== TransactionType.SPECIAL_DIVIDEND);

  private readonly store: Store<AppState> = inject(Store);
  private readonly transactionApi: TransactionApi = inject(TransactionApi);
  private readonly dialogRef: MatDialogRef<TransactionCreateComponent> = inject(MatDialogRef);
  protected readonly transactionPageStore: ReadableTransactionPageStore = inject(transactionPageStore);

  protected readonly depotId: Signal<number> = computed((): number => this.store.selectSignal(selectedDepotIds)()[0]);
  protected readonly depotCurrency: Signal<string> = this.store.selectSignal(selectedDepotCurrency);

  private readonly formModel: WritableSignal<TransactionFormModel> = signal<TransactionFormModel>({
    transactionType: null,
    isSpecialDividend: false,
    securityName: "",
    date: new Date(),
    time: "",
    securityCountOriginal: "",
    securityCountSplitAdjusted: "",
    grossValue: "",
    tax: "",
    fee: "",
  });

  protected readonly form: FieldTree<TransactionFormModel> = form(
    this.formModel,
    (schemaPath: SchemaPathTree<TransactionFormModel>): void => {
      required(schemaPath.transactionType);

      required(schemaPath.securityName);
      pattern(schemaPath.securityName, (): RegExp => {
        const names: string[] = this.transactionPageStore.allSecurityNames();
        return new RegExp(`^(${names.join("|")})$`);
      });

      required(schemaPath.date);

      pattern(schemaPath.time, timeRegex);

      required(schemaPath.securityCountOriginal);
      pattern(schemaPath.securityCountOriginal, countRegex);

      pattern(schemaPath.securityCountSplitAdjusted, countRegex);

      required(schemaPath.grossValue);
      pattern(schemaPath.grossValue, monetaryRegex);

      pattern(schemaPath.tax, monetaryRegex);
      pattern(schemaPath.fee, monetaryRegex);
    },
  );

  protected readonly formValid: Signal<boolean> = computed((): boolean => {
    let valid: boolean = true;
    for (const [, field] of this.form) {
      valid = valid && field().valid();
    }
    return valid;
  });

  protected readonly showTimeField: Signal<boolean> = computed((): boolean => {
    const transactionType: TransactionType | null = this.form.transactionType().value();
    return transactionType === TransactionType.BUY || transactionType === TransactionType.SELL;
  });

  protected readonly filteredSecurityNames: Signal<string[]> = computed((): string[] => {
    const filterValue: string = this.form.securityName().value().trim().toLowerCase();
    return this.transactionPageStore.allSecurityNames()
      .filter((name: string): boolean => name.toLowerCase().includes(filterValue));
  });

  protected readonly netValueInput: Signal<NetValueInput> = computed((): NetValueInput => {
    const values: TransactionFormModel = this.formModel();
    return {
      transactionType: values.transactionType ?? TransactionType.BUY,
      grossValue: values.grossValue,
      tax: values.tax,
      fee: values.fee,
    };
  });

  protected close(): void {
    this.dialogRef.close();
  }

  protected async createTransaction(): Promise<void> {
    if (!this.formValid()) {
      return;
    }

    const values: TransactionFormModel = this.formModel();
    let transactionType: TransactionType = values.transactionType!;
    if (transactionType === TransactionType.DIVIDEND && values.isSpecialDividend) {
      transactionType = TransactionType.SPECIAL_DIVIDEND;
    }

    const year: number = values.date.getFullYear();
    const month: number = values.date.getMonth() + 1;
    const day: number = values.date.getDate();
    const dateString: string = `${year}-${month < 10 ? "0" : ""}${month}-${day < 10 ? "0" : ""}${day}`;

    let time: string | undefined = undefined;
    const buyOrSell: TransactionType[] = [TransactionType.BUY, TransactionType.SELL];
    if (values.time.trim() !== "" && buyOrSell.includes(transactionType)) {
      time = `${values.time}:00`;
    }

    const securityCountOriginal: number = this.getRequiredNumber(values.securityCountOriginal);
    const securityCountSplitAdjusted: number | undefined = this.getOptionalNumber(values.securityCountSplitAdjusted);
    const grossValue: number = this.getRequiredNumber(values.grossValue);
    const tax: number | undefined = this.getOptionalNumber(values.tax);
    const fee: number | undefined = this.getOptionalNumber(values.fee);

    const payload: TransactionCreate = {
      transactionType,
      securityId: this.transactionPageStore.securityIdsByName()[values.securityName],
      date: dateString,
      time,
      securityCountOriginal,
      securityCountSplitAdjusted,
      grossValue,
      tax,
      fee,
    };

    await firstValueFrom(this.transactionApi.createTransaction(this.depotId(), payload));
    this.store.dispatch(DepotActions.reloadDepots());
    this.transactionPageStore.reloadFirstPage();
    this.dialogRef.close(true);
  }

  private getOptionalNumber(value: string): number | undefined {
    const trimmedValue: string = value.trim();
    if (trimmedValue.length > 0) {
      return this.getRequiredNumber(trimmedValue);
    }
    return undefined;
  }

  private getRequiredNumber(value: string): number {
    return parseFloat(value.trim().replace(",", "."));
  }
}
