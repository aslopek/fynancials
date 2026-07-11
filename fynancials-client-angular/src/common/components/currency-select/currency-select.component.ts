import {Component, EventEmitter, OnInit, Output,} from "@angular/core";
import {MatOptionModule} from "@angular/material/core";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {firstValueFrom} from "rxjs";
import {ConfigApi} from "../../../gen/api/configuration";
import {FyCurrencySymbolPipe} from "../../pipe/fy-currency-symbol.pipe";

@Component({
  selector: "app-currency-select",
  imports: [
    MatInputModule,
    MatOptionModule,
    FyCurrencySymbolPipe,
    MatSelectModule,
  ],
  templateUrl: "./currency-select.component.html",
  styleUrl: "./currency-select.component.scss",
})
export class CurrencySelectComponent implements OnInit {
  @Output() onCurrencySelect: EventEmitter<string | undefined> =
    new EventEmitter<string | undefined>();
  protected currencies: string[] = [];
  protected selectedCurrency: string | undefined = undefined;

  constructor(private readonly configApi: ConfigApi) {
  }

  async ngOnInit(): Promise<void> {
    this.currencies = await firstValueFrom(
      this.configApi.getSupportedCurrencies(),
    );
  }

  protected changeCurrency(currency: string | undefined): void {
    this.selectedCurrency = currency;
    this.onCurrencySelect.emit(currency);
  }
}
