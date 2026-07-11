import {Component, DestroyRef, inject, Input, OnChanges, SimpleChanges,} from "@angular/core";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {hideAbsoluteValues} from "../../../store/app-config/app-config.selector";
import {FyCurrencyPipe} from "../../pipe/fy-currency.pipe";
import {FyPercentPipe} from "../../pipe/fy-percent.pipe";
import {AppState} from "../../../store/app.state";

@Component({
  selector: "app-performance-label",
  imports: [FyPercentPipe, FyCurrencyPipe],
  templateUrl: "./performance-label.component.html",
  styleUrl: "./performance-label.component.scss",
})
export class PerformanceLabelComponent implements OnChanges {
  protected _absolutePerformance: number = 0;
  protected _relativePerformance: number | "infinity" = 0;
  protected _currency: string = "EUR";
  protected performanceIsPositive: boolean = true;
  protected absolutePerformanceStyle: string =
    "absolute-performance performance-positive";
  protected relativePerformanceStyle: string =
    "relative-performance performance-positive";

  private readonly appConfigStore: Store<AppState> = inject(Store);
  private readonly hideAbsoluteValues$: Observable<boolean> =
    this.appConfigStore.select(hideAbsoluteValues);
  protected hideAbsoluteValues: boolean = true;

  constructor(destroyRef: DestroyRef) {
    this.hideAbsoluteValues$
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((hideAbsoluteValues) => {
        this.hideAbsoluteValues = hideAbsoluteValues;
      });
  }

  @Input({ required: true })
  set absolutePerformance(absolutePerformance: number) {
    this._absolutePerformance = absolutePerformance;
  }

  get absolutePerformance(): number {
    return this._absolutePerformance;
  }

  @Input({ required: true })
  set relativePerformance(relativePerformance: number | "infinity") {
    this._relativePerformance = relativePerformance;
  }

  get relativePerformance(): number | "infinity" {
    return this._relativePerformance;
  }

  @Input({ required: true })
  set currency(currency: string) {
    this._currency = currency;
  }

  get currency(): string {
    return this._currency;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.performanceIsPositive = this.absolutePerformance >= 0;

    if (this.performanceIsPositive) {
      this.absolutePerformanceStyle =
        "padding absolute-performance performance-positive";
      this.relativePerformanceStyle = "padding performance-positive";
    } else {
      this.absolutePerformanceStyle =
        "padding absolute-performance performance-negative";
      this.relativePerformanceStyle = "padding performance-negative";
    }

    if (this.hideAbsoluteValues) {
      this.relativePerformanceStyle = `${this.relativePerformanceStyle} absolute-performance-hidden`;
    } else {
      this.relativePerformanceStyle = `${this.relativePerformanceStyle} relative-performance`;
    }
  }
}
