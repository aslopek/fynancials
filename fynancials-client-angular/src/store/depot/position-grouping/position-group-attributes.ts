import {SecurityRead} from '../../../gen/api/security';
import {PositionGroupAttribute, PositionGroupAttributeId} from './position-group-attribute.type';
import {PositionGroupBy} from './position-group.type';

export const positionGroupAttributes: readonly PositionGroupAttribute[] = [
  {
    id: 'sector',
    label: 'Sector',
    fallbackGroupName: 'Others',
    resolve: (securities: readonly SecurityRead[]): string | null | undefined => securities[0]?.sector
  }
];

export function getPositionGroupAttribute(id: PositionGroupBy): PositionGroupAttribute | null {
  return positionGroupAttributes.find((attribute: PositionGroupAttribute): boolean => attribute.id === id) ?? null;
}

export const positionGroupByValues: readonly PositionGroupBy[] = [
  'none',
  ...positionGroupAttributes.map((attribute: PositionGroupAttribute): PositionGroupAttributeId => attribute.id)
];
