import {beforeEach, describe, expect, it} from '@jest/globals';
import {getPositionGroupBy, GetPositionGroupByState} from './get-position-group-by.selector';
import {PositionGroupBy} from '../../position-grouping/position-group.type';

describe('getPositionGroupBy', (): void => {
  let state: GetPositionGroupByState;

  beforeEach((): void => {
    state = {position: {groupBy: 'sector'}};
  });

  it('returns the groupBy from state', (): void => {
    const result: PositionGroupBy = getPositionGroupBy(state);
    expect(result).toBe('sector');
  });

  it('returns none when state.position.groupBy is none', (): void => {
    state = {position: {groupBy: 'none'}};

    const result: PositionGroupBy = getPositionGroupBy(state);

    expect(result).toBe('none');
  });
});
