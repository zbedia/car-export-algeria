import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CurrencyService } from '../services/currency.service';
import { TranslationService } from '../services/translation.service';
import { TranslatePipe } from '../pipes/translate.pipe';
import { CurrencyCode, ExchangeRatesResponse, RateType } from '../models/currency.model';

@Component({
  selector: 'app-currency-converter',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './currency-converter.component.html',
  styleUrls: ['./currency-converter.component.css']
})
export class CurrencyConverterComponent implements OnInit {
  rates: ExchangeRatesResponse | null = null;
  loading = false;
  errorMessage = '';

  amount = 10000;
  fromCurrency: CurrencyCode = 'EUR';
  rateType: RateType = 'OFFICIAL';

  constructor(
    private currencyService: CurrencyService,
    private translationService: TranslationService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.currencyService.getRates().subscribe({
      next: (rates) => {
        this.rates = rates;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || this.translationService.t('errors.ratesLoad');
        this.loading = false;
      }
    });
  }

  get toCurrency(): CurrencyCode {
    return this.fromCurrency === 'EUR' ? 'DZD' : 'EUR';
  }

  get activeRate(): number | null {
    if (!this.rates) return null;
    return this.rateType === 'OFFICIAL'
      ? this.rates.officialRateEurToDzd
      : this.rates.parallelRateEurToDzd;
  }

  get convertedAmount(): number | null {
    if (!this.rates || this.activeRate === null) return null;
    if (this.fromCurrency === 'EUR') {
      return this.amount * this.activeRate;
    }
    return this.amount / this.activeRate;
  }

  swapDirection(): void {
    this.fromCurrency = this.fromCurrency === 'EUR' ? 'DZD' : 'EUR';
  }
}
