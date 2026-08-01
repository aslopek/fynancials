import {SecurityRead} from '../../../gen/api/security';

export type PositionGroupAttributeId = 'sector';

export type PositionGroupAttribute = {
  id: PositionGroupAttributeId
  label: string
  fallbackGroupName: string
  resolve: (securities: readonly SecurityRead[]) => string | null | undefined
};
