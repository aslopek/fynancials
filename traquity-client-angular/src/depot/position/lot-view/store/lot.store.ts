import {DepotPositionApi, Lot} from '../../../../gen/api/depot-position';
import {signalStore, withMethods, withState} from '@ngrx/signals';
import {ReadableSignalStore, WritableSignalStore} from '../../../../common/types/signal-store.type';
import {setDepotsAndSecurities, SetDepotsAndSecuritiesFunction} from './methods/set-depots-and-securities';
import {inject} from '@angular/core';
import {Store} from '@ngrx/store';
import {AppState} from '../../../../store/app.state';

type Computed = {};

type Methods = {
  setDepotsAndSecurities: SetDepotsAndSecuritiesFunction
};

export type LotState = {
  depotIds: number[]
  securityIds: number[]
  lots: Lot[]
}

const initialState: LotState = {
  depotIds: [],
  securityIds: [],
  lots: []
};

export type ReadableLotStore = ReadableSignalStore<LotState, Computed, Methods>;

export const lotStore = signalStore(
  withState(initialState),
  withMethods((signalStore: WritableSignalStore<LotState, {}, {}>,
               depotPositionApi: DepotPositionApi = inject(DepotPositionApi),
               globalStore: Store<AppState> = inject(Store)): Methods => {
    return {
      setDepotsAndSecurities: setDepotsAndSecurities(signalStore, depotPositionApi, globalStore)
    };
  })
);