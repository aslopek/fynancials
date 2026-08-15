import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {showLicenseNote} from './show-license-note';

describe('showLicenseNote', (): void => {
  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState});
  });

  it('shows the license note', (): void => {
    showLicenseNote(store);

    expect(getState(store)).toEqual({...initialState, licenseNoteVisible: true});
  });
});
