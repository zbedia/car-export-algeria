import { TestBed } from '@angular/core/testing';
import { ShippingSelectionService } from './shipping-selection.service';

describe('ShippingSelectionService', () => {
  let service: ShippingSelectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShippingSelectionService);
  });

  it('defaults to the MARSEILLE → ALGER route', () => {
    expect(service.originPort()).toBe('MARSEILLE');
    expect(service.destinationPort()).toBe('ALGER');
  });

  it('exposes settable signals', () => {
    service.originPort.set('SETE');
    service.destinationPort.set('ORAN');

    expect(service.originPort()).toBe('SETE');
    expect(service.destinationPort()).toBe('ORAN');
  });
});
