import {Component, inject, Signal,} from "@angular/core";
import {MatButtonModule} from "@angular/material/button";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import html2canvas from "html2canvas-pro";
import {DepotRead} from "../../../gen/api/depot";
import {PositionListComponent} from "../position-list/position-list.component";
import {PositionPieChartComponent} from "../position-pie-chart/position-pie-chart.component";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/app.state";
import {selectedDepots, selectedPositionView, usePositionBuyInValues,} from "../../../store/depot/depot.selector";
import {DepotActions} from "../../../store/depot/depot.actions";

type SelectedPositionView = "donut" | "list";

@Component({
  selector: "app-position-page",
  imports: [
    PositionPieChartComponent,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatButtonToggleModule,
    PositionListComponent,
  ],
  templateUrl: "./position-page.component.html",
  styleUrl: "./position-page.component.scss",
})
export class PositionPageComponent {
  protected readonly selectedDepots: Signal<DepotRead[]>;
  protected readonly useBuyIn: Signal<boolean>;
  protected readonly selectedView: Signal<SelectedPositionView>;
  private readonly store: Store<AppState> = inject(Store);

  constructor() {
    this.selectedDepots = this.store.selectSignal(selectedDepots);
    this.useBuyIn = this.store.selectSignal(usePositionBuyInValues);
    this.selectedView = this.store.selectSignal(selectedPositionView);
  }

  protected toggleUseBuyIn(): void {
    this.store.dispatch(
      DepotActions.setUsePositionBuyInValues({
        useBuyInValues: !this.useBuyIn(),
      }),
    );
  }

  protected selectView(selectedView: SelectedPositionView): void {
    this.store.dispatch(
      DepotActions.setSelectedPositionView({
        selectedView,
      }),
    );
  }

  protected downloadImage(): void {
    const chart: HTMLDivElement = document.getElementById(
      "position-view-container",
    ) as HTMLDivElement;
    chart.classList.add("screenshot");
    html2canvas(chart as HTMLElement, {
      useCORS: true,
      height: chart.scrollHeight,
      backgroundColor: "#303030",
    }).then((canvas) => {
      const link: HTMLAnchorElement = document.createElement("a");
      link.download = `${this.selectedDepots()[0].name} positions.png`;
      link.href = canvas.toDataURL();
      link.click();
    });
    chart.classList.remove("screenshot");
  }
}
