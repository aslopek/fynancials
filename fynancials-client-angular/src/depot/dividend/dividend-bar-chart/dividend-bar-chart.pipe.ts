import {inject, Pipe, PipeTransform} from "@angular/core";
import {AggregatedDividends} from "../store/computed/get-aggregated-dividends";
import type {BarSeriesOption} from 'echarts';
import {EChartsOption} from "echarts";
import {FyCurrencyPipe} from "../../../common";

@Pipe({
  name: "dividendBarChart",
})
export class DividendBarChartPipe implements PipeTransform {

  private readonly fyCurrencyPipe: FyCurrencyPipe = inject(FyCurrencyPipe);

  transform(dividends: AggregatedDividends, hideAbsoluteValues: boolean, currency: string): EChartsOption {
    const seriesLabels: string[] = dividends.years.map((year: number) => `${year}`);
    const seriesData: number[][] = dividends.slices.map((slice): number[] => {
      return slice.aggregated
    });

    const labelOption: BarSeriesOption['label'] = {
      show: true,
      position: 'insideBottom',
      distance: 15,
      align: 'left',
      verticalAlign: 'middle',
      rotate: 90,
      formatter: (params: any) => {
        if (hideAbsoluteValues || params.value === 0) {
          return '';
        } else {
          return `{absolute| ${this.fyCurrencyPipe.transform(params.value, currency)} }`;
        }
      },
      rich: {
        absolute: {
          color: '#4C5058',
          backgroundColor: '#8C8D8E',
          borderRadius: 4,
          align: 'center',
          fontWeight: 'bold',
          fontSize: 14
        }
      }
    };

    let series: BarSeriesOption[];
    if (dividends.timespan === 'year') {
      series = [
        {
          name: 'Dividends',
          type: 'bar',
          label: labelOption,
          emphasis: {focus: 'series'},
          data: dividends.slices.map(s => s.aggregated[0]),
          itemStyle: {
            color: '#4C5058'
          }
        }
      ];
    } else {
      series = seriesData.map((data, i) => ({
        name: seriesLabels[i],
        type: 'bar',
        label: labelOption,
        emphasis: {focus: 'series'},
        data
      }));
    }

    return {
      legend: {
        show: dividends.timespan !== 'year',
        backgroundColor: '#8C8D8E',
        borderColor: '#8C8D8E',
        borderWidth: 2,
        borderRadius: 4
      },
      tooltip: {
        show: dividends.timespan !== 'year',
        valueFormatter: (value) => {
          if (hideAbsoluteValues) {
            return '';
          } else {
            return this.fyCurrencyPipe.transform(value as number, currency);
          }
        },
        trigger: 'axis',
        backgroundColor: '#8C8D8E',
        borderColor: '#8C8D8E',
        borderWidth: 2,
        borderRadius: 4,
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: this.getXAxisLabels(dividends),
        axisTick: {
          show: false
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          show: !hideAbsoluteValues,
          formatter: value => this.fyCurrencyPipe.transform(value, currency, '1.0-0')
        }
      },
      series: series
    };
  }

  private getXAxisLabels(dividends: AggregatedDividends): string[] {
    if (dividends.timespan === 'month') {
      return [
        'January',
        'February',
        'March',
        'April',
        'May',
        'June',
        'July',
        'August',
        'September',
        'October',
        'November',
        'December'
      ];
    } else if (dividends.timespan === 'quarter') {
      return ['Q1', 'Q2', 'Q3', 'Q4'];
    } else {
      return dividends.years.map(year => `${year}`);
    }
  }
}
