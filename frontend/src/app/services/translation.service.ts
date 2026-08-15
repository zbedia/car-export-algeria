import { Inject, Injectable, computed, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Lang, TRANSLATIONS } from '../i18n/translations';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly currentLang = signal<Lang>('en');

  readonly lang = this.currentLang.asReadonly();
  readonly direction = computed<'ltr' | 'rtl'>(() =>
    this.currentLang() === 'ar' ? 'rtl' : 'ltr'
  );

  constructor(@Inject(DOCUMENT) private document: Document) {
    this.applyDocumentAttributes(this.currentLang());
  }

  setLanguage(lang: Lang): void {
    this.currentLang.set(lang);
    this.applyDocumentAttributes(lang);
  }

  t(key: string, params?: Record<string, string | number>): string {
    let text = TRANSLATIONS[this.currentLang()][key] ?? key;
    if (params) {
      for (const [paramKey, value] of Object.entries(params)) {
        text = text.replace(new RegExp(`\\{${paramKey}\\}`, 'g'), String(value));
      }
    }
    return text;
  }

  private applyDocumentAttributes(lang: Lang): void {
    this.document.documentElement.lang = lang;
    this.document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
  }
}
