import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../services/translation.service';
import { Lang } from '../i18n/translations';

interface LanguageOption {
  code: Lang;
  flag: string;
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
    { code: 'fr', flag: '🇫🇷', label: 'Français' },
    { code: 'en', flag: '🇬🇧', label: 'English' },
    { code: 'ar', flag: '🇩🇿', label: 'العربية' }
  ];

  constructor(public translationService: TranslationService) {}

  select(lang: Lang): void {
    this.translationService.setLanguage(lang);
  }
}
