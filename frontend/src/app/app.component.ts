import { Component } from '@angular/core';
import { VehicleSearchComponent } from './vehicle-search/vehicle-search.component';
import { LanguageSwitcherComponent } from './language-switcher/language-switcher.component';
import { AppFooterComponent } from './app-footer/app-footer.component';
import { WhatsAppButtonComponent } from './whatsapp-button/whatsapp-button.component';
import { TranslatePipe } from './pipes/translate.pipe';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [VehicleSearchComponent, LanguageSwitcherComponent, AppFooterComponent, WhatsAppButtonComponent, TranslatePipe],
  template: `
    <header class="app-header">
      <div class="header-content">
        <img class="header-logo" src="assets/logo/logo_fennec_car_export_modif.svg" alt="DZautoFinder" />
        <div class="header-text">
          <h1>{{ 'app.title' | translate }}</h1>
          <p>{{ 'app.subtitle' | translate }}</p>
        </div>
        <app-language-switcher></app-language-switcher>
      </div>
    </header>
    <main>
      <app-vehicle-search></app-vehicle-search>
    </main>
    <app-footer></app-footer>
    <app-whatsapp-button></app-whatsapp-button>
  `
})
export class AppComponent {}
