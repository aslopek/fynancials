import {Component, OnDestroy, Signal,} from "@angular/core";
import {Store} from "@ngrx/store";
import {NgxEchartsDirective} from "ngx-echarts";
import {FyCurrencyPipe, FyDecimalPipe, FyPercentPipe} from "../../../common";
import {hideAbsoluteValues} from "../../../store/app-config/app-config.selector";
import {ECharts} from "echarts/core";
import {AppState} from "../../../store/app.state";
import {positions, selectedDepotCurrency, usePositionBuyInValues,} from "../../../store/depot/depot.selector";
import {Positions} from "../../../store/depot/depot.state";
import {PositionPieChartPipe} from "./position-pie-chart.pipe";

@Component({
  selector: "app-position-pie-chart",
  imports: [FyCurrencyPipe, NgxEchartsDirective, PositionPieChartPipe],
  providers: [FyCurrencyPipe, FyDecimalPipe, FyPercentPipe],
  templateUrl: "./position-pie-chart.component.html",
  styleUrl: "./position-pie-chart.component.scss",
})
export class PositionPieChartComponent implements OnDestroy {
  private chartInstance: ECharts | null = null;

  protected readonly hideAbsoluteValues: Signal<boolean>;
  protected readonly currency: Signal<string>;
  protected readonly useBuyIn: Signal<boolean>;
  protected readonly depotComposition: Signal<Positions>;

  constructor(store: Store<AppState>) {
    this.hideAbsoluteValues = store.selectSignal(hideAbsoluteValues);
    this.currency = store.selectSignal(selectedDepotCurrency);
    this.useBuyIn = store.selectSignal(usePositionBuyInValues);
    this.depotComposition = store.selectSignal(positions);
  }

  ngOnDestroy(): void {
    if (this.chartInstance != null) {
      this.chartInstance.dispose();
    }
  }

  protected onChartInit(e: ECharts): void {
    this.chartInstance = e;
  }
}
