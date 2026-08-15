import {Signal} from '@angular/core';
import {WritableStateSource} from '@ngrx/signals';

export type ReadableSignalStore<State extends object, Computed extends object = {}, Methods extends object = {}>
  = Computed & Methods & {
  [Key in keyof State]: Signal<State[Key]>
}

export type WritableSignalStore<State extends object, Computed extends object = {}, Methods extends object = {}>
  = ReadableSignalStore<State, Computed, Methods> & WritableStateSource<State>;
