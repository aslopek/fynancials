import {Pipe, PipeTransform} from '@angular/core';

export interface TextToken {
  text: string;
  isScript: boolean;
}

@Pipe({
  name: 'tokenizeScript',
  standalone: true
})
export class ScriptTokenizerPipe implements PipeTransform {
  transform(value: string | undefined | null): TextToken[] {
    if (!value) {
      return [];
    }

    const scriptRegex: RegExp = /(#\w+\((?:[^()]+|\([^()]*\))*\))/g;
    const parts: string[] = value.split(scriptRegex);
    return parts.map((part: string): TextToken => {
      return {
        text: part,
        isScript: part.startsWith('#')
      };
    });
  }
}
