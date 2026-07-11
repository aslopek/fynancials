import {Component, DestroyRef, effect, inject, input, InputSignal, signal, WritableSignal,} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {FyDatePipe, FyDecimalPipe} from "../../../common";
import {ReadableStockSplitStore, StockSplitParams, stockSplitStore,} from "./store/stock-split.store";
import {takeUntilDestroyed, toObservable} from "@angular/core/rxjs-interop";
import {FieldTree, form, FormField} from "@angular/forms/signals";

@Component({
  selector: "app-stock-split",
  imports: [
    FormsModule,
    FyDatePipe,
    FyDecimalPipe,
    MatInputModule,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatCheckboxModule,
    FormField,
  ],
  providers: [stockSplitStore],
  templateUrl: "./stock-split.component.html",
  styleUrl: "./stock-split.component.scss",
})
export class StockSplitComponent {
  readonly securityId: InputSignal<number> = input.required<number>();
  protected readonly stockSplitStore: ReadableStockSplitStore =
    inject(stockSplitStore);

  protected readonly formModel: WritableSignal<StockSplitParams>;
  protected readonly stockSplitForm: FieldTree<StockSplitParams>;

  constructor(destroyRef: DestroyRef) {
    toObservable(this.securityId)
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((securityId: number): void => {
        this.stockSplitStore.loadStockSplits(securityId);
      });

    this.formModel = signal<StockSplitParams>({
      ...this.stockSplitStore.stockSplitParams(),
    });

    toObservable(this.stockSplitStore.stockSplitParams)
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((stockSplitParams: StockSplitParams) => {
        this.formModel.set(stockSplitParams);
      });

    this.stockSplitForm = form(this.formModel);

    effect(() => {
      this.stockSplitStore.setStockSplitParams(this.formModel());
    });
  }
}
