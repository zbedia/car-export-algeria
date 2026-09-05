import { Component } from '@angular/core';
import { TranslatePipe } from '../pipes/translate.pipe';
import { TranslationService } from '../services/translation.service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './app-footer.component.html',
  styleUrls: ['./app-footer.component.css']
})
export class AppFooterComponent {
  readonly carXportUrl = 'https://carxexport.com/fr/offers';
  readonly exportCar213Url = 'https://exportcar213.com/inventaire';
  readonly contactEmail = 'contact@dzautoimport.com';
  readonly whatsappUrl = 'https://wa.me/21355000000';
  readonly currentYear = new Date().getFullYear();

  get rightsText(): string {
    return this.translationService.t('footer.rights', {
      year: this.currentYear,
      app: this.translationService.t('app.title')
    });
  }

  constructor(private translationService: TranslationService) {}
}