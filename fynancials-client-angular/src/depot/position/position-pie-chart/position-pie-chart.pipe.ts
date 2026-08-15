import {Pipe, PipeTransform} from "@angular/core";
import {FyCurrencyPipe, FyDecimalPipe, FyPercentPipe} from "../../../common";
import {DepotPosition} from "../../../gen/api/depot-position";
import {SecurityLogoUrlPipe} from "../../../common/pipe/security-logo-url.pipe";
import {EChartsOption} from "echarts";
import {positionPieChartGeometry} from "./position-pie-chart-geometry";

@Pipe({
  name: 'positionPieChart',
  pure: true
})
export class PositionPieChartPipe implements PipeTransform {

  constructor(private readonly fyCurrencyPipe: FyCurrencyPipe, private readonly fyPercentPipe: FyPercentPipe,
              private readonly fyDecimalPipe: FyDecimalPipe, private readonly securityLogoUrlPipe: SecurityLogoUrlPipe) {
  }

  transform(
    positions: DepotPosition[],
    hideAbsoluteValues: boolean,
    currency: string,
    useBuyIn: boolean,
    groupingActive: boolean
  ): EChartsOption {
    if (!positions || positions.length === 0) {
      return {series: []};
    }

    const data = positions.map((pos: DepotPosition) => {
      const relative: string = this.fyPercentPipe.transform(
        useBuyIn ? pos.buyInRelative : pos.currentSizeRelative
      );

      const absoluteCurrency: string = this.fyCurrencyPipe.transform(
        useBuyIn ? pos.buyInAbsolute : pos.currentSizeAbsolute,
        currency
      );

      const absoluteCount: string = this.fyDecimalPipe.transform(pos.count, '1.0-3');
      const logoUrl: string = this.securityLogoUrlPipe.transform(pos.securityIds[0]);
      const logoRichOverride = {
        rich: {
          logo: {
            width: 18,
            height: 18,
            borderRadius: 4,
            backgroundColor: {image: logoUrl}
          }
        }
      };

      return {
        name: pos.displayName,
        value: useBuyIn ? pos.buyInAbsolute : pos.currentSizeAbsolute,
        relativeSize: relative,
        absoluteSize: hideAbsoluteValues
          ? ''
          : ` ${absoluteCurrency} (${absoluteCount}) `,
        label: logoRichOverride
      };
    });

    const labelFormatter = (params: any) =>
      `{logo|} {name| ${params.name}:} {size|${params.data.absoluteSize}} {relativeSize| ${params.data.relativeSize} } `;

    const label = {
      formatter: labelFormatter,
      backgroundColor: '#1E1E1E',
      borderColor: '#5A5F66',
      borderWidth: 2,
      borderRadius: 4,
      shadowColor: 'rgba(0, 0, 0, 0.5)',
      shadowBlur: 8,
      padding: [6, 10],
      rich: {
        logo: {
          width: 18,
          height: 18,
          borderRadius: 4,
          align: 'center' as const,
          verticalAlign: 'middle' as const
        },
        name: {
          color: '#ffffff',
          align: 'center' as const,
          fontSize: 14,
          fontWeight: 500,
          lineHeight: 33
        },
        size: {
          color: '#ffffff',
          align: 'center' as const
        },
        relativeSize: {
          align: 'center' as const,
          color: '#fff',
          backgroundColor: '#4C5058',
          padding: [4, 4, 4, 4],
          borderRadius: 4
        }
      }
    };
    const labelLine = {length: 100};

    return {
      series: [
        {
          name: '',
          type: 'pie',
          radius: [
            `${positionPieChartGeometry.doughnutInnerRadius * 2}%`,
            `${positionPieChartGeometry.doughnutOuterRadius * 2}%`
          ],
          startAngle: positionPieChartGeometry.pieStartAngleDegrees,
          data,
          ...(groupingActive
            ? {
              label: {show: false},
              labelLine: {show: false},
              emphasis: {
                label: {show: false},
                labelLine: {show: false}
              }
            }
            : {
              label,
              labelLine
            }),
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
