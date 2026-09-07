import { TestBed } from '@angular/core/testing';
import { TranslationService } from './translation.service';
import { DOCUMENT } from '@angular/common';

describe('TranslationService', () => {
  let service: TranslationService;
  let document: Document;

  beforeEach(() => {
    window.localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TranslationService);
    document = TestBed.inject(DOCUMENT);
  });

  it('defaults to French', () => {
    expect(service.lang()).toBe('fr');
    expect(service.t('app.title')).toBe('Export Voitures Algérie');
  });

  it('returns the key itself when the translation is missing', () => {
    expect(service.t('no.such.key')).toBe('no.such.key');
  });

  it('substitutes every {param} placeholder in the string', () => {
    const text = service.t('discountReason.SMALL_ENGINE', {
      fuel: 'Essence',
      threshold: '1600',
      displacement: '1598'
    });
    expect(text).toContain('Essence');
    expect(text).toContain('1600');
    expect(text).toContain('1598');
    expect(text).not.toContain('{fuel}');
  });

  it('switches language and updates document lang/dir', () => {
    service.setLanguage('en');
    expect(document.documentElement.lang).toBe('en');
    expect(document.documentElement.dir).toBe('ltr');
    expect(service.t('app.title')).toBe('Car Export Algeria');

    service.setLanguage('ar');
    expect(document.documentElement.dir).toBe('rtl');
  });

  it('persists the chosen language and restores it on a new instance', () => {
    service.setLanguage('en');
    expect(window.localStorage.getItem('car-export-lang')).toBe('en');

    // A fresh instance reads the persisted value back.
    const second = new TranslationService(document);
    expect(second.lang()).toBe('en');
  });

  it('exposes the locale mapped to the current language', () => {
    service.setLanguage('fr');
    expect(service.locale).toBe('fr-FR');
    service.setLanguage('ar');
    expect(service.locale).toBe('ar-DZ');
  });
});
