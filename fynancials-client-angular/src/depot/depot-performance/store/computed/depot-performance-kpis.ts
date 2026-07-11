import {ReadableSignalStore} from "../../../../common/types/signal-store.type";
import {DepotPerformanceState} from "../depot-performance.store";
import {computed, Signal} from "@angular/core";
import {RebasedDepotValue} from "./rebased-depot-value.type";

export type DepotPerformanceKpis = {
  investedCapital: number
  cashPosition: number
  cashPositionRelativeToAbsoluteValue: number | 'infinity'
  absoluteValue: number
  dateRange: {
    growthAbsolute: number
    growthRelative: number | 'infinity'
  }
};

const emptyKpis: DepotPerformanceKpis = {
  investedCapital: 0,
  cashPosition: 0,
  cashPositionRelativeToAbsoluteValue: 0,
  absoluteValue: 0,
  dateRange: {
    growthAbsolute: 0,
    growthRelative: 0
  }
} as const;

export function depotPerformanceKpis(signalStore: ReadableSignalStore<DepotPerformanceState>,
                                     depotValuesSignal: Signal<RebasedDepotValue[]>,
                                     isRebasedSignal: Signal<boolean>): Signal<DepotPerformanceKpis> {
  const addCashToAbsoluteValue: Signal<boolean> = signalStore.addCashToAbsoluteValue;

  return computed<DepotPerformanceKpis>((): DepotPerformanceKpis => {
    const depotValues: RebasedDepotValue[] = depotValuesSignal();
    if (depotValues.length === 0) {
      return emptyKpis;
    }

    const first: RebasedDepotValue = depotValues[0];
    const last: RebasedDepotValue = depotValues[depotValues.length - 1];
    const isRebased: boolean = isRebasedSignal();
    // with a single data point in view there's no "during the window" to measure growth over
    const isSingleDataPoint: boolean = depotValues.length === 1;

    let cashPositionRelativeToAbsoluteValue: number | 'infinity';
    if (last.absoluteValue === 0) {
      cashPositionRelativeToAbsoluteValue = 'infinity';
    } else if (addCashToAbsoluteValue()) {
      cashPositionRelativeToAbsoluteValue = last.cashPosition / (last.absoluteValue - last.cashPosition);
    } else {
      cashPositionRelativeToAbsoluteValue = last.cashPosition / last.absoluteValue;
    }

    return {
      investedCapital: isRebased ? last.investedCapital - first.investedCapital : last.investedCapital,
      cashPosition: last.cashPosition,
      cashPositionRelativeToAbsoluteValue,
      absoluteValue: last.absoluteValue,
      dateRange: {
        growthAbsolute: isSingleDataPoint ? 0 : last.performanceAbsolute,
        growthRelative: isSingleDataPoint ? 0 : last.performanceRelative
      }
    };
  });
}
