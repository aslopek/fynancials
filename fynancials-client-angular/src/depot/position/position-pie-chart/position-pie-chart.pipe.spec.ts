import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {EChartsOption} from 'echarts';
import {PositionPieChartPipe} from './position-pie-chart.pipe';
import {FyCurrencyPipe, FyDecimalPipe, FyPercentPipe} from '../../../common';
import {SecurityLogoUrlPipe} from '../../../common/pipe/security-logo-url.pipe';
import {DepotPosition} from '../../../gen/api/depot-position';
import {depotPositionFactory} from '../../../testing';

type LabelState = {
  show?: boolean
  length?: number
};

type PieSeries = {
  data: { name: string }[]
  label: LabelState
  labelLine: LabelState
  emphasis?: { label: LabelState, labelLine: LabelState }
};

function seriesOf(options: EChartsOption): PieSeries {
  return (options.series as PieSeries[])[0];
}

describe('PositionPieChartPipe', (): void => {
  let positions: DepotPosition[];
  let pipe: PositionPieChartPipe;

  beforeEach((): void => {
    positions = [
      depotPositionFactory({securityIds: [1], displayName: 'Small', currentSizeAbsolute: 100}),
      depotPositionFactory({securityIds: [2], displayName: 'Large', currentSizeAbsolute: 900})
    ];

    pipe = new PositionPieChartPipe(
      {transform: jest.fn((): string => '$1,000.00')} as unknown as FyCurrencyPipe,
      {transform: jest.fn((): string => '10.00%')} as unknown as FyPercentPipe,
      {transform: jest.fn((): string => '10')} as unknown as FyDecimalPipe,
      {transform: jest.fn((): string => '/securities/logo')} as unknown as SecurityLogoUrlPipe
    );
  });

  it('shows the per-position labels and their leader lines while grouping is inactive', (): void => {
    const series: PieSeries = seriesOf(pipe.transform(positions, false, 'USD', false, false));
    expect(series.label.show).toBeUndefined();
    expect(series.labelLine.length).toBe(100);
  });

  it('hides the per-position labels and their leader lines while grouping is active', (): void => {
    const series: PieSeries = seriesOf(pipe.transform(positions, false, 'USD', false, true));
    expect(series.label.show).toBe(false);
    expect(series.labelLine.show).toBe(false);
    expect(series.emphasis?.label.show).toBe(false);
    expect(series.emphasis?.labelLine.show).toBe(false);
  });

  it('keeps the incoming position order, so a group\'s segments stay contiguous', (): void => {
    const series: PieSeries = seriesOf(pipe.transform(positions, false, 'USD', false, true));
    expect(series.data.map((item: { name: string }): string => item.name)).toEqual(['Small', 'Large']);
  });

  it('returns an empty series for no positions', (): void => {
    expect(pipe.transform([], false, 'USD', false, false)).toEqual({series: []});
  });
});
