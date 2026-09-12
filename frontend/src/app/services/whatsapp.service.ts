import { Injectable, signal } from '@angular/core';

/**
 * Shared entry point for the floating WhatsApp contact button. Holds the
 * phone number used for every chat and remembers the last vehicle the user
 * consulted (i.e. whose listing they opened) so the pre-filled message can
 * carry that vehicle's reference.
 */
@Injectable({ providedIn: 'root' })
export class WhatsAppService {
  readonly phoneNumber = '21355000000';

  readonly consultedReference = signal<string | null>(null);

  consult(reference: string): void {
    this.consultedReference.set(reference);
  }
}