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
  readonly autoExportMarseilleUrl = 'https://autoexportmarseille.com/';
  readonly contactEmail = 'contact@dzautoimport.com';
  readonly currentYear = new Date().getFullYear();

  get rightsText(): string {
    return this.translationService.t('footer.rights', {
      year: this.currentYear,
      app: 'DZautoFinder'
    });
  }

  constructor(private translationService: TranslationService) {}
}
