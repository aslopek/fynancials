import {inject, Pipe, PipeTransform} from "@angular/core";
import {EChartsOption} from "echarts";
import {FyCurrencyPipe, FyDatePipe, FyPercentPipe} from "../../../common";
import {HistoricalSecurityPrice} from "../../../gen/api/historical-security-price";

@Pipe({
  name: "historicalPriceChart",
})
export class HistoricalPriceChartPipe implements PipeTransform {

  private readonly fyCurrencyPipe: FyCurrencyPipe = inject(FyCurrencyPipe);
  private readonly fyDatePipe: FyDatePipe = inject(FyDatePipe);
  private readonly fyPercentPipe: FyPercentPipe = inject(FyPercentPipe);

  transform(prices: HistoricalSecurityPrice[], percent: boolean): EChartsOption {
    if (!prices || prices.length === 0) {
      return {};
    }

    const currency: string = prices[0].currency;
    const basePrice: number = prices[0].price;
    const hasPositiveDirection: boolean = prices[prices.length - 1].price >= basePrice;
    const lineColor: string = hasPositiveDirection ? '#22c55e' : '#ef4444';
    const areaColor: string = hasPositiveDirection ? 'rgba(34, 197, 94,' : 'rgba(239, 68, 68,';

    const dates: string[] = [];
    const values: number[] = [];
    const rawPrices: { [date: string]: number } = {};
    const absoluteChange: { [date: string]: number } = {};
    const relativeChange: { [date: string]: number } = {};
    let formattedDate: string;

    for (const item of prices) {
      formattedDate = this.formatDate(item.date);
      dates.push(formattedDate);
      values.push(percent ? (item.price / basePrice - 1) * 100 : item.price);
      rawPrices[formattedDate] = item.price;
      absoluteChange[formattedDate] = item.price - basePrice;
      relativeChange[formattedDate] = item.price / basePrice - 1;
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

          const date: string = (params[0] as { name: string }).name;
          const rawChange: number = absoluteChange[date] ?? 0;
          const changeColor: string = rawChange >= 0 ? '#22c55e' : '#ef4444';

          const absoluteLabel: string = `${rawChange >= 0 ? '+' : ''}${this.formatCurrency(rawChange, currency)}`;
          const relativeLabel: string = `${rawChange >= 0 ? '+' : ''}${this.formatPercent(relativeChange[date] ?? 0)}`;

          return `
            <div style="font-weight: 600; margin-bottom: 6px; color: rgba(255, 255, 255, 0.5); font-size: 11px;">${this.escapeHtml(date)}</div>

            <div style="display: flex; justify-content: space-between; gap: 24px; margin-bottom: 4px;">
              <span style="color: rgba(255, 255, 255, 0.7);">Price:</span>
              <strong style="font-variant-numeric: tabular-nums; color: #ffffff;">${this.formatCurrency(rawPrices[date] ?? 0, currency)}</strong>
            </div>

            <div style="display: flex; justify-content: space-between; gap: 24px; margin-top: 6px; border-top: 1px dashed rgba(255, 255, 255, 0.1); padding-top: 4px;">
              <span style="color: rgba(255, 255, 255, 0.7);">${rawChange >= 0 ? 'Growth' : 'Decline'}:</span>
              <strong style="color: ${changeColor}; font-variant-numeric: tabular-nums;">${absoluteLabel}</strong>
            </div>
            <div style="display: flex; justify-content: space-between; gap: 24px;">
              <span></span>
              <strong style="color: ${changeColor}; font-variant-numeric: tabular-nums;">${relativeLabel}</strong>
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
          formatter: (value: number): string => percent ? `${value.toFixed(0)}%` : this.formatCurrency(value, currency)
        }
      },

      series: [
        {
          name: 'Price',
          type: 'line',
          data: values,
          showSymbol: false,
          smooth: 0.2,
          lineStyle: {width: 3, color: lineColor},
          areaStyle: {
            color: {
              type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                {offset: 0, color: `${areaColor} 0.16)`},
                {offset: 1, color: `${areaColor} 0)`}
              ]
            }
          }
        }
      ]
    };
  }

  private formatCurrency(value: number, currency: string): string {
    return this.fyCurrencyPipe.transform(value, currency);
  }

  private formatDate(date: string): string {
    return this.fyDatePipe.transform(date);
  }

  private formatPercent(value: number): string {
    return this.fyPercentPipe.transform(value);
  }

  private escapeHtml(value: string): string {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }
}
