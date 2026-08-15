import { Component } from '@angular/core';
import { VehicleSearchComponent } from './vehicle-search/vehicle-search.component';
import { CurrencyConverterComponent } from './currency-converter/currency-converter.component';
import { ShippingEstimatorComponent } from './shipping-estimator/shipping-estimator.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [VehicleSearchComponent, CurrencyConverterComponent, ShippingEstimatorComponent],
  template: `
    <header class="app-header">
      <h1>Car Export Algeria</h1>
      <p>Comparison tool for vehicles under 3 years old</p>
    </header>
    <main>
      <app-currency-converter></app-currency-converter>
      <app-shipping-estimator></app-shipping-estimator>
      <app-vehicle-search></app-vehicle-search>
    </main>
  `
})
export class AppComponent {}
