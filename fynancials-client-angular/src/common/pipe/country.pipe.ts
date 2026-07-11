import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'country',
  standalone: true
})
export class CountryPipe implements PipeTransform {

  transform(countryCode: string): string {
    if (countryCode.length !== 2) {
      return countryCode;
    }

    const codePoints: number[] = countryCode
    .toUpperCase()
    .split('')
    .map(char => char.charCodeAt(0) + 127397);
    return String.fromCodePoint(...codePoints);
  }
}
