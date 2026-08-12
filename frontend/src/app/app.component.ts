import { Component } from '@angular/core';
import { VehicleSearchComponent } from './vehicle-search/vehicle-search.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [VehicleSearchComponent],
  template: `
    <header class="app-header">
      <h1>Car Export Algeria</h1>
      <p>Comparison tool for vehicles under 3 years old</p>
    </header>
    <main>
      <app-vehicle-search></app-vehicle-search>
    </main>
  `
})
export class AppComponent {}
