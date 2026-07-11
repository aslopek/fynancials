import {AsyncPipe} from "@angular/common";
import {Component, inject, Signal,} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {LetDirective} from "@ngrx/component";
import {Store} from "@ngrx/store";
import {Observable} from "rxjs";
import {FyCurrencyPipe, FyDecimalPipe, FyPercentPipe} from "../../../common";
import {FyIconComponent} from "../../../common/components/fy-icon/fy-icon.component";
import {PerformanceLabelComponent} from "../../../common/components/performance-label/performance-label.component";
import {SecurityLogoApi} from "../../../gen/api/security";
import {hideAbsoluteValues} from "../../../store/app-config/app-config.selector";
import {AppState} from "../../../store/app.state";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {LotsDialogComponent, LotsDialogData,} from "../lots-dialog/lots-dialog.component";
import {IncomeByPosition, Positions} from "../../../store/depot/depot.state";
import {incomeByPosition, positions, selectedDepotCurrency, selectedDepotIds, usePositionBuyInValues,} from "../../../store/depot/depot.selector";

@Component({
  selector: "app-position-list",
  imports: [
    FyCurrencyPipe,
    FyPercentPipe,
    FyIconComponent,
    FyDecimalPipe,
    MatButtonModule,
    MatIconModule,
    PerformanceLabelComponent,
    MatTooltipModule,
    LetDirective,
    AsyncPipe,
  ],
  templateUrl: "./position-list.component.html",
  styleUrl: "./position-list.component.scss",
})
export class PositionListComponent {
  protected readonly basePath: string;

  protected readonly depotCurrency: Signal<string>;
  protected readonly useBuyIn: Signal<boolean>;
  protected readonly depotComposition: Signal<Positions>;
  protected readonly depotIds: Signal<number[]>;
  protected readonly income: Signal<IncomeByPosition>;

  protected readonly hideAbsoluteValues$: Observable<boolean> =
    this.store.select(hideAbsoluteValues);

  private readonly dialog: MatDialog = inject(MatDialog);

  constructor(
    private readonly store: Store<AppState>,
    securityLogoApi: SecurityLogoApi,
  ) {
    this.basePath = securityLogoApi.configuration.basePath ?? "";
    this.depotCurrency = this.store.selectSignal(selectedDepotCurrency);
    this.useBuyIn = this.store.selectSignal(usePositionBuyInValues);
    this.depotComposition = this.store.selectSignal(positions);
    this.depotIds = this.store.selectSignal(selectedDepotIds);
    this.income = this.store.selectSignal(incomeByPosition);
  }

  protected openLotsDialog(securityIds: number[]): void {
    const lotsDialog: MatDialogRef<LotsDialogComponent> =
      this.dialog.open(LotsDialogComponent, {
        height: "90%",
        width: "30%",
        minHeight: "15em",
        minWidth: "5em",
        panelClass: "mat-app-background",
        autoFocus: false,
        disableClose: true,
        data: {
          depotIds: this.depotIds(),
          securityIds,
        } satisfies LotsDialogData,
      });
  }
}
