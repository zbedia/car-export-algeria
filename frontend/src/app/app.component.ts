import { Component } from '@angular/core';
import { VehicleSearchComponent } from './vehicle-search/vehicle-search.component';
import { CurrencyConverterComponent } from './currency-converter/currency-converter.component';
import { ShippingEstimatorComponent } from './shipping-estimator/shipping-estimator.component';
import { LanguageSwitcherComponent } from './language-switcher/language-switcher.component';
import { TranslatePipe } from './pipes/translate.pipe';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    VehicleSearchComponent,
    CurrencyConverterComponent,
    ShippingEstimatorComponent,
    LanguageSwitcherComponent,
    TranslatePipe
  ],
  template: `
    <header class="app-header">
      <div class="header-content">
        <div class="header-text">
          <h1>{{ 'app.title' | translate }}</h1>
          <p>{{ 'app.subtitle' | translate }}</p>
        </div>
        <app-language-switcher></app-language-switcher>
      </div>
    </header>
    <main>
      <app-currency-converter></app-currency-converter>
      <app-shipping-estimator></app-shipping-estimator>
      <app-vehicle-search></app-vehicle-search>
    </main>
  `
})
export class AppComponent {}
