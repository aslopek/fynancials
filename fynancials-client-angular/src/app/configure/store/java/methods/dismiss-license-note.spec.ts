import {beforeEach, describe, expect, it} from '@jest/globals';
import {getState, signalState, SignalState} from '@ngrx/signals';
import {ConfigureStoreState, initialState} from '../../configure.store';
import {dismissLicenseNote} from './dismiss-license-note';

describe('dismissLicenseNote', (): void => {
  let store: SignalState<ConfigureStoreState>;

  beforeEach((): void => {
    store = signalState<ConfigureStoreState>({...initialState, licenseNoteVisible: true});
  });

  it('dismisses the license note', (): void => {
    dismissLicenseNote(store);

    expect(getState(store)).toEqual({...initialState, licenseNoteVisible: false});
  });
});
