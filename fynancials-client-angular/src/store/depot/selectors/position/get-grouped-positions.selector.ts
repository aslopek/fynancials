import {Positions} from '../../depot.state';
import {DepotPosition} from '../../../../gen/api/depot-position';
import {SecuritiesById} from '../../../security/security.state';
import {SecurityRead} from '../../../../gen/api/security';
import {GroupedPositions, PositionGroup, PositionGroupBy} from '../../position-grouping/position-group.type';
import {getPositionGroupAttribute} from '../../position-grouping/position-group-attributes';
import {PositionGroupAttribute} from '../../position-grouping/position-group-attribute.type';

export function getGroupedPositions(positions: Positions, securities: SecuritiesById, groupBy: PositionGroupBy,
                                    useBuyIn: boolean): GroupedPositions {
  const attribute: PositionGroupAttribute | null = getPositionGroupAttribute(groupBy);
  if (groupBy === 'none' || attribute == null) {
    return {positions: positions.positions, groups: []};
  }

  const groupsByValue: { [value: string]: PositionGroup } = {};
  const groupOrder: PositionGroup[] = [];
  let fallbackGroup: PositionGroup | null = null;
  let securitiesOfPosition: SecurityRead[];
  let value: string | null | undefined;
  let group: PositionGroup;

  for (const position of positions.positions) {
    securitiesOfPosition = position.securityIds
      .map((id: number): SecurityRead | undefined => securities[id])
      .filter((security: SecurityRead | undefined): security is SecurityRead => security != null);
    value = attribute.resolve(securitiesOfPosition);

    if (value == null || value.trim() === '') {
      if (fallbackGroup == null) {
        fallbackGroup = createEmptyGroup(attribute.fallbackGroupName);
        groupOrder.push(fallbackGroup);
      }
      group = fallbackGroup;
    } else {
      if (groupsByValue[value] == null) {
        groupsByValue[value] = createEmptyGroup(value);
        groupOrder.push(groupsByValue[value]);
      }
      group = groupsByValue[value];
    }

    group.positions.push(position);
    group.buyInAbsolute += position.buyInAbsolute;
    group.buyInRelative += position.buyInRelative;
    group.currentSizeAbsolute += position.currentSizeAbsolute;
    group.currentSizeRelative += position.currentSizeRelative;
  }

  groupOrder.sort((a: PositionGroup, b: PositionGroup): number =>
    useBuyIn ? b.buyInAbsolute - a.buyInAbsolute : b.currentSizeAbsolute - a.currentSizeAbsolute
  );

  return {
    positions: groupOrder.flatMap((group: PositionGroup): DepotPosition[] => group.positions),
    groups: groupOrder
  };
}

function createEmptyGroup(name: string): PositionGroup {
  return {
    name,
    positions: [],
    buyInAbsolute: 0,
    buyInRelative: 0,
    currentSizeAbsolute: 0,
    currentSizeRelative: 0
  };
}
