import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../services/translation.service';
import { Lang } from '../i18n/translations';

interface LanguageOption {
  code: Lang;
  flagUrl: string;
  label: string;
}

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './language-switcher.component.html',
  styleUrls: ['./language-switcher.component.css']
})
export class LanguageSwitcherComponent {
  languages: LanguageOption[] = [
    { code: 'fr', flagUrl: 'https://flagcdn.com/fr.svg', label: 'Français' },
    { code: 'en', flagUrl: 'https://flagcdn.com/gb.svg', label: 'English' },
    { code: 'ar', flagUrl: 'https://flagcdn.com/dz.svg', label: 'العربية' }
  ];

  constructor(public translationService: TranslationService) {}

  select(lang: Lang): void {
    this.translationService.setLanguage(lang);
  }
}
