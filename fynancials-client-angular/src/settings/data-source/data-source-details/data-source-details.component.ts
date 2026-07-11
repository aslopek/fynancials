import {Component, computed, inject, input, InputSignal, signal, Signal, WritableSignal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatChipsModule} from '@angular/material/chips';
import {DataSourceStore, ReadableDataSourceStore} from "../store/data-source.store";
import {
  AnyDataSource,
  CurrencyMapping,
  DataSourceVariant,
  DateConfiguration,
  MultiUrlDataSource,
  RequestHeader,
  SingleUrlDataSource,
  UrlPattern,
  ZonedTime
} from "../data-source.type";
import {FyDecimalPipe} from "../../../common";
import {ScriptTokenizerPipe} from "./tokenize-script.pipe";
import {MatButton} from "@angular/material/button";
import {Store} from "@ngrx/store";
import {AppState} from "../../../store/app.state";
import {SecurityActions} from "../../../store/security/security.actions";
import {DividendAnnouncementActions} from "../../../store/dividend-announcement/dividend-announcement.actions";

type UrlPatternRow = {
  timespanInDays?: number
  urlPattern: string
};

const emptyHistoricalSecurityPriceDataSource: MultiUrlDataSource = {
  name: 'New Data Source',
  urlPatterns: [],
  requestHeaders: [],
  jsonPathDate: '',
  dateFormat: {
    format: 'CUSTOM_STRING',
    customPattern: 'yyyy-MM-dd'
  },
  jsonPathValue: '',
  currencyMappings: [],
  marketCloseTimes: []
} as const;

const emptyDividendAnnouncementDataSource: SingleUrlDataSource = {
  name: 'New Data Source',
  urlPattern: '',
  requestHeaders: [],
  jsonPathDate: '',
  dateFormat: {
    format: 'CUSTOM_STRING',
    customPattern: 'yyyy-MM-dd'
  },
  jsonPathValue: '',
  currencyMappings: []
} as const;

@Component({
  selector: "app-data-source-details",
  imports: [
    CommonModule,
    MatCardModule,
    MatDividerModule,
    MatListModule,
    MatTableModule,
    MatIconModule,
    MatChipsModule,
    FyDecimalPipe,
    ScriptTokenizerPipe,
    MatButton
  ],
  templateUrl: "./data-source-details.component.html",
  styleUrl: "./data-source-details.component.scss",
})
export class DataSourceDetailsComponent {

  readonly dataSourceVariant: InputSignal<DataSourceVariant> = input.required<DataSourceVariant>();

  protected readonly headerColumns = ['name', 'value'];
  protected readonly currencyColumns = ['key', 'code', 'multiplier'];
  protected readonly marketCloseColumns = ['time', 'timezone'];
  protected readonly urlPatternColumns: Signal<string[]> = computed<string[]>((): string[] => {
    return this.dataSourceVariant() === 'historical-security-price' ? ['timespan', 'pattern'] : ['pattern'];
  });
  private readonly dataSourceStore: ReadableDataSourceStore = inject(DataSourceStore);
  protected readonly selectedDataSourceId: Signal<number | null> = this.dataSourceStore.selectedDataSourceId;
  protected readonly selectedDataSource: Signal<AnyDataSource> = computed<AnyDataSource>(() => {
    const uploaded: AnyDataSource | null = this.uploadedDataSource();
    if (uploaded != null) {
      return uploaded;
    }

    const selectedDataSource: AnyDataSource | null = this.dataSourceStore.selectedDataSource();
    if (selectedDataSource != null) {
      return selectedDataSource;
    }
    return this.dataSourceVariant() === 'historical-security-price'
      ? emptyHistoricalSecurityPriceDataSource
      : emptyDividendAnnouncementDataSource;
  });
  protected readonly urlPatternRows: Signal<UrlPatternRow[]> = computed<UrlPatternRow[]>((): UrlPatternRow[] => {
    const source: AnyDataSource = this.selectedDataSource();
    if (source.urlPatterns != null) {
      return source.urlPatterns;
    }
    if (source.urlPattern != null && source.urlPattern.length > 0) {
      return [{urlPattern: source.urlPattern}];
    }
    return [];
  });
  private readonly uploadedDataSource: WritableSignal<AnyDataSource | null> = signal<AnyDataSource | null>(null);
  protected readonly isSaveDisabled: Signal<boolean> = computed<boolean>((): boolean => this.uploadedDataSource() === null);
  private readonly store: Store<AppState> = inject(Store);

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file: File = input.files[0];
    const reader = new FileReader();

    reader.onload = (e: ProgressEvent<FileReader>): void => {
      try {
        const json: any = JSON.parse(e.target?.result as string);
        const {id, version, ...dataSourceData} = json;
        if (this.dataSourceVariant() === 'historical-security-price') {
          if (this.isValidMultiUrlDataSource(dataSourceData)) {
            this.uploadedDataSource.set(dataSourceData);
          }
        } else if (this.isValidSingleUrlDataSource(dataSourceData)) {
          this.uploadedDataSource.set(dataSourceData);
        }
      } catch (error) {
      }
    };

    reader.readAsText(file);
    input.value = '';
  }

  protected save(): void {
    const data: AnyDataSource | null = this.uploadedDataSource();
    if (data == null) {
      return;
    }
    const selectedDataSourceId: number | null = this.selectedDataSourceId();
    const variant: DataSourceVariant = this.dataSourceVariant();

    if (variant === 'historical-security-price' && this.isValidMultiUrlDataSource(data)) {
      const dataSource: MultiUrlDataSource = {
        ...data,
        marketCloseTimes: data.marketCloseTimes ?? []
      };
      this.store.dispatch(SecurityActions.setHistoricalSecurityPriceDataSource({
        id: selectedDataSourceId ?? undefined,
        dataSource: dataSource,
      }));
    } else if (variant === 'dividend-announcement' && this.isValidSingleUrlDataSource(data)) {
      this.store.dispatch(DividendAnnouncementActions.setDividendAnnouncementDataSource({
        id: selectedDataSourceId ?? undefined,
        dataSource: data,
      }));
    }
    this.uploadedDataSource.set(null);
  }

  private isValidDataSource(obj: any): boolean {
    return (
      obj != null &&
      typeof obj.name === 'string' &&
      typeof obj.jsonPathDate === 'string' &&
      typeof obj.jsonPathValue === 'string' &&
      this.isValidDateFormat(obj.dateFormat) &&
      Array.isArray(obj.requestHeaders) && obj.requestHeaders.every((x: any) => this.isValidRequestHeader(x)) &&
      Array.isArray(obj.currencyMappings) && obj.currencyMappings.every((x: any) => this.isValidCurrencyMapping(x)) &&
      this.isValidMarketCloseTimeConfiguration(obj) &&
      (obj.jsonPathCurrency === undefined || typeof obj.jsonPathCurrency === 'string') &&
      (obj.regexCurrency === undefined || typeof obj.regexCurrency === 'string') &&
      (obj.regexCurrencyGroup === undefined || typeof obj.regexCurrencyGroup === 'number')
    );
  }

  private isValidMultiUrlDataSource(obj: any): obj is MultiUrlDataSource {
    return (
      this.isValidDataSource(obj) &&
      Array.isArray(obj.urlPatterns) && obj.urlPatterns.every((x: unknown): x is UrlPattern => this.isValidUrlPattern(x)) &&
      obj.urlPattern === undefined
    );
  }

  private isValidSingleUrlDataSource(obj: any): obj is SingleUrlDataSource {
    return (
      this.isValidDataSource(obj) &&
      typeof obj.urlPattern === 'string' && obj.urlPattern.length > 0 &&
      obj.urlPatterns === undefined
    );
  }

  private isValidMarketCloseTimeConfiguration(obj: any): boolean {
    if (this.dataSourceVariant() === 'historical-security-price') {
      return obj.marketCloseTimes === undefined || obj.marketCloseTimes === null ||
        (Array.isArray(obj.marketCloseTimes) && obj.marketCloseTimes.every((x: unknown): x is ZonedTime => this.isValidZonedTime(x)));
    }
    return obj.marketCloseTimes === undefined;
  }

  private isValidDateFormat(obj: any): obj is DateConfiguration {
    return (
      obj != null &&
      (obj.format === 'TIMESTAMP_SECONDS' || obj.format === 'TIMESTAMP_MILLISECONDS' || obj.format === 'CUSTOM_STRING') &&
      (obj.customPattern === undefined || typeof obj.customPattern === 'string')
    );
  }

  private isValidUrlPattern(obj: any): obj is UrlPattern {
    return (
      obj != null &&
      typeof obj.timespanInDays === 'number' &&
      typeof obj.urlPattern === 'string'
    );
  }

  private isValidRequestHeader(obj: any): obj is RequestHeader {
    return (
      obj != null &&
      typeof obj.headerName === 'string' &&
      typeof obj.headerValue === 'string'
    );
  }

  private isValidCurrencyMapping(obj: any): obj is CurrencyMapping {
    return (
      obj != null &&
      typeof obj.currencyKey === 'string' &&
      typeof obj.mappedCurrencyCode === 'string' &&
      (obj.multiplier === undefined || typeof obj.multiplier === 'number')
    );
  }

  private isValidZonedTime(obj: any): obj is ZonedTime {
    return (
      obj != null &&
      typeof obj.time === 'string' &&
      typeof obj.timeZone === 'string'
    );
  }
}
