import {inject, Pipe, PipeTransform} from "@angular/core";
import {FyCurrencyPipe, FyDecimalPipe, FyPercentPipe} from "../../../common";
import {DepotPosition} from "../../../gen/api/depot-position";
import {EChartsOption} from "echarts";

@Pipe({
  name: 'positionPieChart',
  pure: true
})
export class PositionPieChartPipe implements PipeTransform {

  private readonly fyCurrencyPipe = inject(FyCurrencyPipe);
  private readonly fyPercentPipe = inject(FyPercentPipe);
  private readonly fyDecimalPipe = inject(FyDecimalPipe);

  transform(
    positions: DepotPosition[],
    hideAbsoluteValues: boolean,
    currency: string,
    useBuyIn: boolean
  ): EChartsOption {

    if (!positions || positions.length === 0) {
      return {series: []};
    }

    const sorted = [...positions].sort((a, b) =>
      useBuyIn
        ? b.buyInAbsolute - a.buyInAbsolute
        : b.currentSizeAbsolute - a.currentSizeAbsolute
    );

    const data = sorted.map(pos => {
      const relative = this.fyPercentPipe.transform(
        (useBuyIn ? pos.buyInRelative : pos.currentSizeRelative) / 100
      );

      const absoluteCurrency = this.fyCurrencyPipe.transform(
        useBuyIn ? pos.buyInAbsolute : pos.currentSizeAbsolute,
        currency
      );

      const absoluteCount = this.fyDecimalPipe.transform(pos.count, '1.0-3');

      return {
        name: pos.displayName,
        value: useBuyIn ? pos.buyInAbsolute : pos.currentSizeAbsolute,
        relativeSize: relative,
        absoluteSize: hideAbsoluteValues
          ? ''
          : ` ${absoluteCurrency} (${absoluteCount}) `
      };
    });

    const labelFormatter = (params: any) =>
      `{name| ${params.name}:} {size|${params.data.absoluteSize}} {relativeSize| ${params.data.relativeSize} } `;

    return {
      series: [
        {
          name: '',
          type: 'pie',
          radius: ['60%', '80%'],
          startAngle: 0,
          data,
          labelLine: {length: 100},
          label: {
            formatter: labelFormatter,
            backgroundColor: '#8C8D8E',
            borderColor: '#8C8D8E',
            borderWidth: 2,
            borderRadius: 4,
            rich: {
              name: {
                color: '#4C5058',
                align: 'center',
                fontSize: 14,
                fontWeight: 500,
                lineHeight: 33
              },
              size: {
                color: '#4C5058',
                align: 'center'
              },
              relativeSize: {
                align: 'center',
                color: '#fff',
                backgroundColor: '#4C5058',
                padding: [4, 4, 4, 4],
                borderRadius: 4
              }
            }
          },
          itemStyle: {
            borderRadius: 0,
            borderColor: '#fff',
            borderWidth: 2
          }
        }
      ]
    };
  }
}

