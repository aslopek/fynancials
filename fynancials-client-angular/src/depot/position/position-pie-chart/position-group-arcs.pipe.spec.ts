import {beforeEach, describe, expect, it, jest} from '@jest/globals';
import {PositionGroupArc, PositionGroupArcsPipe} from './position-group-arcs.pipe';
import {PositionGroupTextPipe} from '../position-group-text.pipe';
import {PositionGroup} from '../../../store/depot/position-grouping/position-group.type';
import {positionPieChartGeometry} from './position-pie-chart-geometry';

function groupOf(currentSizeAbsolute: number, name: string = 'Group'): PositionGroup {
  return {
    name,
    positions: [],
    buyInAbsolute: currentSizeAbsolute,
    buyInRelative: 0,
    currentSizeAbsolute,
    currentSizeRelative: 0
  };
}

type ParsedArc = {
  radius: number
  largeArc: number
  sweep: number
  fromAngle: number
  toAngle: number
};

// the pipe draws every arc on a circle around the 50/50 center of the 100x100 viewBox, with 0 degrees at 12 o'clock
// and angles growing clockwise - so parseArc turns the two endpoints of a path back into the angles they were drawn
// from, which is what the assertions below are actually about
function parseArc(d: string): ParsedArc {
  const match: RegExpMatchArray | null = d.match(
    /^M ([\d.-]+) ([\d.-]+) A ([\d.-]+) [\d.-]+ 0 (\d) (\d) ([\d.-]+) ([\d.-]+)$/
  );
  if (match == null) {
    throw new Error(`unparseable arc path: ${d}`);
  }
  return {
    radius: Number(match[3]),
    largeArc: Number(match[4]),
    sweep: Number(match[5]),
    fromAngle: angleAt(Number(match[1]), Number(match[2])),
    toAngle: angleAt(Number(match[6]), Number(match[7]))
  };
}

function angleAt(x: number, y: number): number {
  return (Math.atan2(x - 50, 50 - y) * 180 / Math.PI + 360) % 360;
}

// only valid for a clockwise arc, i.e. one drawn with sweep 1
function midpointAngle(arc: ParsedArc): number {
  return (arc.fromAngle + ((arc.toAngle - arc.fromAngle + 360) % 360) / 2) % 360;
}

describe('PositionGroupArcsPipe', (): void => {
  let positionGroupTextPipe: PositionGroupTextPipe;
  let transform: jest.Mock<(group: PositionGroup, useBuyIn: boolean, hideAbsoluteValues: boolean, includeAbsolute: boolean,
                            currency: string) => string>;
  let pipe: PositionGroupArcsPipe;

  beforeEach((): void => {
    transform = jest.fn(
      (_group: PositionGroup, _useBuyIn: boolean, _hideAbsoluteValues: boolean, _includeAbsolute: boolean, _currency: string): string =>
        'Full Text'
    );
    positionGroupTextPipe = {transform} as unknown as PositionGroupTextPipe;
    pipe = new PositionGroupArcsPipe(positionGroupTextPipe);
  });

  it('returns an empty array when the total size is 0', (): void => {
    const result: PositionGroupArc[] = pipe.transform([groupOf(0), groupOf(0)], false, false, 'USD');
    expect(result).toEqual([]);
  });

  describe('three groups spanning less than, equal to and more than half the circle', (): void => {
    let groups: PositionGroup[];
    let result: PositionGroupArc[];

    beforeEach((): void => {
      groups = [groupOf(100, 'Small A'), groupOf(100, 'Small B'), groupOf(800, 'Large')];
      result = pipe.transform(groups, false, false, 'USD');
    });

    it('returns one arc per group, with ids in order', (): void => {
      expect(result.map((arc: PositionGroupArc): string => arc.id)).toEqual([
        'position-group-arc-0',
        'position-group-arc-1',
        'position-group-arc-2'
      ]);
    });

    it('draws every hairline at the configured hairline radius', (): void => {
      result.forEach((arc: PositionGroupArc): void => {
        expect(parseArc(arc.hairlinePath).radius).toBe(positionPieChartGeometry.hairlineRadius);
      });
    });

    it('insets every arc by the configured gap at each end, so neighbours are twice that far apart', (): void => {
      const gapDegrees: number = parseArc(result[1].hairlinePath).fromAngle - parseArc(result[0].hairlinePath).toAngle;
      expect(gapDegrees).toBeCloseTo(2 * positionPieChartGeometry.arcGapDegrees, 1);
    });

    it('sets the large-arc flag only for the group spanning more than 180 degrees', (): void => {
      expect(parseArc(result[0].hairlinePath).largeArc).toBe(0);
      expect(parseArc(result[1].hairlinePath).largeArc).toBe(0);
      expect(parseArc(result[2].hairlinePath).largeArc).toBe(1);
    });

    it('sweeps hairlines clockwise', (): void => {
      result.forEach((arc: PositionGroupArc): void => {
        expect(parseArc(arc.hairlinePath).sweep).toBe(1);
      });
    });

    it('reverses the label path and uses the flipped radius for a group whose midpoint sits in the lower half', (): void => {
      const midpoint: number = midpointAngle(parseArc(result[0].hairlinePath));
      expect(midpoint).toBeGreaterThan(90);
      expect(midpoint).toBeLessThan(270);

      const label: ParsedArc = parseArc(result[0].labelPath);
      expect(label.sweep).toBe(0);
      expect(label.radius).toBe(positionPieChartGeometry.labelRadiusFlipped);
    });

    it('keeps the label path forward and uses the regular radius for a group whose midpoint sits in the upper half', (): void => {
      const midpoint: number = midpointAngle(parseArc(result[2].hairlinePath));
      expect(midpoint).toBeGreaterThan(270);

      const label: ParsedArc = parseArc(result[2].labelPath);
      expect(label.sweep).toBe(1);
      expect(label.radius).toBe(positionPieChartGeometry.labelRadius);
    });

    it('draws the hover band from inside the hairline to beyond that arc\'s own label radius', (): void => {
      result.forEach((arc: PositionGroupArc): void => {
        const band: ParsedArc = parseArc(arc.hoverPath);
        const innerEdge: number = band.radius - arc.hoverStrokeWidth / 2;
        const outerEdge: number = band.radius + arc.hoverStrokeWidth / 2;
        expect(innerEdge).toBeLessThan(positionPieChartGeometry.hairlineRadius);
        expect(outerEdge).toBeGreaterThan(parseArc(arc.labelPath).radius);
      });
    });

    it('always returns the full tooltip text, regardless of truncation', (): void => {
      result.forEach((arc: PositionGroupArc): void => {
        expect(arc.tooltip).toBe('Full Text');
      });
    });

    it('builds the text once per group, including the absolute size, and uses it for both the label and the tooltip', (): void => {
      expect(transform).toHaveBeenCalledTimes(groups.length);
      expect(transform).toHaveBeenNthCalledWith(1, groups[0], false, false, true, 'USD');
      expect(transform).toHaveBeenNthCalledWith(2, groups[1], false, false, true, 'USD');
      expect(transform).toHaveBeenNthCalledWith(3, groups[2], false, false, true, 'USD');
    });
  });

  describe('a group too narrow for the full label', (): void => {
    let result: PositionGroupArc[];

    beforeEach((): void => {
      transform.mockImplementation((): string => 'ABCDEFGHIJKLMNOPQRST');
      result = pipe.transform([groupOf(950, 'Large'), groupOf(50, 'Narrow')], false, false, 'USD');
    });

    it('truncates the label with an ellipsis', (): void => {
      expect(result[1].label).toBe('ABCDEFGHI…');
    });

    it('still returns the untruncated tooltip', (): void => {
      expect(result[1].tooltip).toBe('ABCDEFGHIJKLMNOPQRST');
    });
  });

  describe('a group too narrow for any label text', (): void => {
    it('shows no label at all, but still returns the untruncated tooltip', (): void => {
      const result: PositionGroupArc[] = pipe.transform([groupOf(999, 'Large'), groupOf(1, 'Sliver')], false, false, 'USD');
      expect(result[1].label).toBe('');
      expect(result[1].tooltip).toBe('Full Text');
    });
  });
});
