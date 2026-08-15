import { Pipe, PipeTransform, inject } from '@angular/core';
import { TranslationService } from '../services/translation.service';

/**
 * Impure by design: re-evaluates on every change detection cycle so the
 * whole UI updates instantly when the language is switched, without
 * needing to manually refresh each binding.
 */
@Pipe({
  name: 'translate',
  standalone: true,
  pure: false
})
export class TranslatePipe implements PipeTransform {
  private translationService = inject(TranslationService);

  transform(key: string): string {
    return this.translationService.t(key);
  }
}
