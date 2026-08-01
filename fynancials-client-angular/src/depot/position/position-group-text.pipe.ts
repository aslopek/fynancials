import {Pipe, PipeTransform} from "@angular/core";
import {FyCurrencyPipe, FyPercentPipe} from "../../common";
import {PositionGroup} from "../../store/depot/position-grouping/position-group.type";

@Pipe({
  name: 'positionGroupText',
  pure: true
})
export class PositionGroupTextPipe implements PipeTransform {

  constructor(private readonly fyCurrencyPipe: FyCurrencyPipe, private readonly fyPercentPipe: FyPercentPipe) {
  }

  transform(group: PositionGroup, useBuyIn: boolean, hideAbsoluteValues: boolean, includeAbsolute: boolean, currency: string): string {
    const relative: string = this.fyPercentPipe.transform((useBuyIn ? group.buyInRelative : group.currentSizeRelative) / 100);

    if (!includeAbsolute || hideAbsoluteValues) {
      return `${group.name} · ${relative}`;
    }

    const absolute: string = this.fyCurrencyPipe.transform(useBuyIn ? group.buyInAbsolute : group.currentSizeAbsolute, currency);
    return `${group.name} · ${relative} · ${absolute}`;
  }
}
