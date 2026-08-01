import {DepotPosition} from '../../../gen/api/depot-position';
import {PositionGroupAttributeId} from './position-group-attribute.type';

export type PositionGroupBy = 'none' | PositionGroupAttributeId;

export type PositionGroup = {
  name: string
  positions: DepotPosition[]
  buyInAbsolute: number
  buyInRelative: number
  currentSizeAbsolute: number
  currentSizeRelative: number
};

export type GroupedPositions = {
  positions: DepotPosition[]
  groups: PositionGroup[]
};
