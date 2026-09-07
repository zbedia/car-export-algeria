import { Inject, Injectable, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { Lang, TRANSLATIONS } from '../i18n/translations';

const STORAGE_KEY = 'car-export-lang';
const DEFAULT_LANG: Lang = 'fr';

export const LOCALE_BY_LANG: Record<Lang, string> = {
  fr: 'fr-FR',
  en: 'en-GB',
  ar: 'ar-DZ'
};

function isLang(value: string | null): value is Lang {
  return value === 'en' || value === 'fr' || value === 'ar';
}

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly currentLang = signal<Lang>(this.initialLang());

  readonly lang = this.currentLang.asReadonly();

  get locale(): string {
    return LOCALE_BY_LANG[this.currentLang()];
  }

  constructor(@Inject(DOCUMENT) private document: Document) {
    this.applyDocumentAttributes(this.currentLang());
  }

  setLanguage(lang: Lang): void {
    this.currentLang.set(lang);
    this.persist(lang);
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

  private initialLang(): Lang {
    const stored = this.readPersistedLang();
    return stored ?? DEFAULT_LANG;
  }

  private readPersistedLang(): Lang | null {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      return isLang(stored) ? stored : null;
    } catch {
      return null;
    }
  }

  private persist(lang: Lang): void {
    try {
      window.localStorage.setItem(STORAGE_KEY, lang);
    } catch {
      // Storage may be unavailable (private mode, blocked cookies) — the
      // in-memory signal still drives the UI, persistence is best-effort.
    }
  }

  private applyDocumentAttributes(lang: Lang): void {
    this.document.documentElement.lang = lang;
    this.document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
  }
}
