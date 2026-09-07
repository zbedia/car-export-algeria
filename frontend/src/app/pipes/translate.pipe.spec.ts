import { TestBed } from '@angular/core/testing';
import { TranslatePipe } from './translate.pipe';
import { TranslationService } from '../services/translation.service';

describe('TranslatePipe', () => {
  let pipe: TranslatePipe;
  let translationService: TranslationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    translationService = TestBed.inject(TranslationService);
    // Other specs may have persisted a different language in localStorage —
    // reset to the default so these assertions are deterministic.
    translationService.setLanguage('fr');
    pipe = TestBed.runInInjectionContext(() => new TranslatePipe());
  });

  it('translates a plain key', () => {
    expect(pipe.transform('search.button')).toBe('Rechercher');
  });

  it('substitutes named params', () => {
    expect(pipe.transform('search.bestPriceAt', { source: 'CarXport' })).toBe('Meilleur prix chez CarXport');
  });

  it('returns the key for an unknown translation', () => {
    expect(pipe.transform('unknown.key')).toBe('unknown.key');
  });

  it('re-evaluates after the language changes', () => {
    expect(pipe.transform('app.title')).toBe('Export Voitures Algérie');
    translationService.setLanguage('en');
    expect(pipe.transform('app.title')).toBe('Car Export Algeria');
  });
});
