import {Component, inject, Signal} from "@angular/core";
import {DepotPerformanceStore, ReadableDepotPerformanceStore} from "../store/depot-performance.store";
import {MatCard, MatCardContent, MatCardHeader, MatCardSubtitle} from "@angular/material/card";
import {FyCurrencyPipe, FyDatePipe, FyPercentPipe} from "../../../common";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/app.state";
import {selectedDepotCurrency} from "../../../store/depot/depot.selector";
import {hideAbsoluteValues} from "../../../store/app-config/app-config.selector";
import {ObfuscatedKpiComponent} from "./obfuscated-kpi/obfuscated-kpi.component";
import {DataRanges} from "../store/computed/data-ranges";
import {DepotPerformanceKpis} from "../store/computed/depot-performance-kpis";

@Component({
  selector: "app-depot-performance-kpis",
  imports: [
    MatCard,
    MatCardContent,
    MatCardSubtitle,
    MatCardHeader,
    FyCurrencyPipe,
    FyPercentPipe,
    ObfuscatedKpiComponent,
    FyDatePipe
  ],
  templateUrl: "./depot-performance-kpis.component.html",
  styleUrl: "./depot-performance-kpis.component.scss",
})
export class DepotPerformanceKpisComponent {

  private readonly depotPerformanceStore: ReadableDepotPerformanceStore = inject(DepotPerformanceStore);
  protected readonly dataRanges: Signal<DataRanges> = this.depotPerformanceStore.dataRanges;
  protected readonly kpis: Signal<DepotPerformanceKpis> = this.depotPerformanceStore.kpis;
  protected readonly currency: Signal<string>;
  protected readonly hideAbsoluteValues: Signal<boolean>;
  protected readonly xirr: Signal<number> = this.depotPerformanceStore.extendedInternalRateOfReturns;

  constructor(store: Store<AppState>) {
    this.currency = store.selectSignal(selectedDepotCurrency);
    this.hideAbsoluteValues = store.selectSignal(hideAbsoluteValues);
  }
}
