import {inject, Pipe, PipeTransform} from "@angular/core";
import {EChartsOption} from "echarts";
import {RebasedDepotValue} from "../store/computed/rebased-depot-value.type";
import {FyCurrencyPipe, FyDatePipe, FyPercentPipe} from "../../../common";
import {BenchmarkResult} from "../store/benchmark/benchmark.type";

@Pipe({
  name: "depotPerformanceChart",
})
export class DepotPerformanceChartPipe implements PipeTransform {

  private readonly fyCurrencyPipe: FyCurrencyPipe = inject(FyCurrencyPipe);
  private readonly fyDatePipe: FyDatePipe = inject(FyDatePipe);
  private readonly fyPercentPipe: FyPercentPipe = inject(FyPercentPipe);
  private readonly primaryColorRgb: { r: number, g: number, b: number } = this.readPrimaryColorRgb();

  transform(
    positions: RebasedDepotValue[],
    hideAbsoluteValues: boolean,
    currency: string,
    showCapitalInvested: boolean,
    showHorizontalIndicator: boolean,
    benchmark: BenchmarkResult | [BenchmarkResult, BenchmarkResult] | null
  ): EChartsOption {
    if (!positions || positions.length === 0) {
      return {};
    }

    const dates: string[] = [];
    const absoluteValues: number[] = [];
    const investedCapital: number[] = [];
    const performanceAbsolute: { [date: string]: number } = {};
    const performanceRelative: { [date: string]: number | 'infinity' } = {};
    let capitalInvested: boolean = false;
    let formattedDate: string;

    for (const item of positions) {
      formattedDate = this.formatDate(item.date);
      dates.push(formattedDate);
      absoluteValues.push(item.absoluteValue);
      investedCapital.push(item.investedCapital);
      performanceAbsolute[formattedDate] = item.performanceAbsolute;
      performanceRelative[formattedDate] = item.performanceRelative;
      if (item.investedCapital > 0) {
        capitalInvested = true;
      }
    }

    const {r, g, b}: { r: number, g: number, b: number } = this.primaryColorRgb;
    const series: NonNullable<EChartsOption['series']> = [
      {
        name: 'Depot Value',
        type: 'line',
        data: absoluteValues,
        showSymbol: false,
        smooth: 0.2,
        lineStyle: {width: 3, color: `rgb(${r}, ${g}, ${b})`},
        areaStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              {offset: 0, color: `rgba(${r}, ${g}, ${b}, 0.16)`},
              {offset: 1, color: `rgba(${r}, ${g}, ${b}, 0)`}
            ]
          }
        }
      }
    ];

    const displayCapitalInvestedSeries: boolean = showCapitalInvested && capitalInvested;
    if (displayCapitalInvestedSeries) {
      series.push({
        name: 'Invested',
        type: 'line',
        data: investedCapital,
        showSymbol: false,
        smooth: 0.2,
        lineStyle: {
          width: 1.5,
          type: 'dashed',
          color: 'rgba(255, 255, 255, 0.25)'
        }
      });
    }

    if (showHorizontalIndicator) {
      const startValue: number = absoluteValues[0] ?? 0;
      const horizontalLineData: number[] = new Array(absoluteValues.length).fill(startValue);
      series.push({
        name: 'Start Value Indicator',
        type: 'line',
        data: horizontalLineData,
        showSymbol: false,
        silent: true,
        lineStyle: {
          width: 1,
          type: 'dashed',
          color: 'rgba(255, 255, 255, 0.25)'
        }
      });
    }

    if (benchmark) {
      const benchmarkList: BenchmarkResult[] = Array.isArray(benchmark) ? benchmark : [benchmark];
      const benchmarkColors: string[] = ['#f97316', '#a855f7'];

      benchmarkList.forEach((bench: BenchmarkResult, index: number): void => {
        series.push({
          name: bench.name,
          type: 'line',
          data: bench.values,
          showSymbol: false,
          smooth: 0.2,
          lineStyle: {
            width: 2,
            type: 'solid',
            color: benchmarkColors[index] ?? '#10b981'
          }
        });
      });
    }

    return {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(28, 28, 30, 0.95)',
        borderColor: 'rgba(255, 255, 255, 0.08)',
        borderWidth: 1,
        textStyle: {
          color: '#ffffff',
          fontSize: 13
        },
        padding: [10, 14],
        extraCssText: 'box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3); border-radius: 8px;',
        formatter: (params: unknown): string => {
          if (!Array.isArray(params) || params.length === 0) return '';

          const date: string = params[0].name;
          const rawProfit: number = performanceAbsolute[date] ?? 0;
          const profitColor: string = rawProfit >= 0 ? '#22c55e' : '#ef4444';

          let seriesHtml: string = '';

          params.forEach((p: { seriesName: string, value?: number }): void => {
            const seriesName: string = p.seriesName;
            const rawValue: number = p.value ?? 0;

            if (seriesName === 'Start Value Indicator') {
              return;
            }

            const formattedValue: string = hideAbsoluteValues ? '••••••' : this.formatCurrency(rawValue, currency);

            seriesHtml += `
              <div style="display: flex; justify-content: space-between; gap: 24px; margin-bottom: 4px;">
                <span style="color: rgba(255, 255, 255, 0.7);">${this.escapeHtml(seriesName)}:</span>
                <strong style="font-variant-numeric: tabular-nums; color: #ffffff;">${formattedValue}</strong>
              </div>
            `;
          });

          const absolutePerformance: string = hideAbsoluteValues ? '••••••' : `${rawProfit >= 0 ? '+' : ''}${this.formatCurrency(rawProfit, currency)}`;
          const relativePerformance: string = `${rawProfit >= 0 ? '+' : ''}${this.formatPercent(performanceRelative[date])}`;
          return `
            <div style="font-weight: 600; margin-bottom: 6px; color: rgba(255, 255, 255, 0.5); font-size: 11px;">${this.escapeHtml(date)}</div>
            
            ${seriesHtml}
            
            <div style="display: flex; justify-content: space-between; gap: 24px; margin-top: 6px; border-top: 1px dashed rgba(255, 255, 255, 0.1); padding-top: 4px;">
              <span style="color: rgba(255, 255, 255, 0.7);">${rawProfit >= 0 ? 'Growth' : 'Decline'}:</span>
              <strong style="color: ${profitColor}; font-variant-numeric: tabular-nums;">${absolutePerformance}</strong>
            </div>
            <div style="display: flex; justify-content: space-between; gap: 24px;">
              <span></span>
              <strong style="color: ${profitColor}; font-variant-numeric: tabular-nums;">${relativePerformance}</strong>
            </div>
          `;
        }
      },

      grid: {
        left: '1%',
        right: '1%',
        bottom: '1%',
        top: '4%',
        containLabel: true
      },

      xAxis: {
        type: 'category',
        data: dates,
        boundaryGap: false,
        axisLine: {lineStyle: {color: 'rgba(255, 255, 255, 0.08)'}},
        axisTick: {show: false},
        axisLabel: {
          color: 'rgba(255, 255, 255, 0.4)',
          fontSize: 11,
          margin: 12
        }
      },

      yAxis: {
        type: 'value',
        scale: true,
        axisLine: {show: false},
        axisTick: {show: false},
        splitLine: {lineStyle: {color: 'rgba(255, 255, 255, 0.04)'}},
        axisLabel: {
          color: 'rgba(255, 255, 255, 0.4)',
          fontSize: 11,
          formatter: (value: number): string => hideAbsoluteValues ? '••••' : this.formatCurrency(value, currency)
        }
      },
      series
    };
  }

  /**
   * `--fy-color-primary` is set in styles.scss from the Material theme's primary palette, so this
   * stays in sync with the theme instead of hardcoding a second copy of the color here.
   */
  private readPrimaryColorRgb(): { r: number, g: number, b: number } {
    const hex: string = getComputedStyle(document.documentElement).getPropertyValue('--fy-color-primary').trim();
    const match: RegExpMatchArray | null = hex.match(/^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i);
    if (!match) {
      return {r: 59, g: 130, b: 246};
    }
    return {r: parseInt(match[1], 16), g: parseInt(match[2], 16), b: parseInt(match[3], 16)};
  }

  private formatCurrency(value: number, currency: string): string {
    return this.fyCurrencyPipe.transform(value, currency);
  }

  private formatDate(date: string): string {
    return this.fyDatePipe.transform(date);
  }

  private formatPercent(value: number | 'infinity'): string {
    return value === 'infinity' ? '∞' : this.fyPercentPipe.transform(value);
  }

  /**
   * The tooltip formatter builds HTML via string interpolation for echarts. seriesName/date are controlled today (fixed labels,
   * formatted dates), but escape them anyway so this stays safe if a future feature (e.g. user-named benchmarks) makes seriesName
   * attacker-influenced.
   */
  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}
