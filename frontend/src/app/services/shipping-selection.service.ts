import { Injectable, signal } from '@angular/core';
import { DestinationPort, OriginPort } from '../models/shipping.model';

/**
 * Holds the currently selected shipping route so it can be shared between
 * the standalone shipping estimator widget and the per-vehicle "Estimate
 * shipping cost" links, without forcing the user to re-pick ports on
 * every single vehicle card.
 */
@Injectable({ providedIn: 'root' })
export class ShippingSelectionService {
  readonly originPort = signal<OriginPort>('MARSEILLE');
  readonly destinationPort = signal<DestinationPort>('ALGER');
}
