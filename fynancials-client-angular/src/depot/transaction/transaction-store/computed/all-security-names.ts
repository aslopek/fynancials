import {computed, Signal} from '@angular/core';

export function allSecurityNames(securityIdsByName: Signal<{ [securityName: string]: number }>): Signal<string[]> {
  return computed((): string[] => Object.keys(securityIdsByName()).sort());
}
