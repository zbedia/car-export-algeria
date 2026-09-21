import { TestBed } from '@angular/core/testing';
import { FavoritesService } from './favorites.service';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';

function listing(id: number, overrides: Partial<VehicleSearchResult> = {}): VehicleSearchResult {
  return {
    id,
    source: 'CarXport',
    externalUrl: `https://example.com/${id}`,
    brand: 'Peugeot',
    model: '308',
    year: 2023,
    mileageKm: 20000,
    price: 15000,
    currency: 'EUR',
    garageCity: 'Stockholm',
    bestPrice: false,
    cheapestSource: null,
    fuelType: 'ESSENCE' as FuelType,
    engineDisplacementCm3: 1199,
    engineDisplacementThresholdCm3: 1500,
    customsDiscountPercentage: 50,
    customsDiscountReasonCode: 'SMALL_ENGINE',
    imageUrl: null,
    ageMonths: 10,
    customs: null,
    ...overrides
  };
}

describe('FavoritesService', () => {
  let service: FavoritesService;

  beforeEach(() => {
    service = TestBed.inject(FavoritesService);
  });

  it('is initially empty', () => {
    expect(service.count).toBe(0);
    expect(service.isFavorite(1)).toBeFalse();
  });

  it('adds a vehicle on toggle and marks it as favorite', () => {
    service.toggle(listing(1));

    expect(service.count).toBe(1);
    expect(service.isFavorite(1)).toBeTrue();
    expect(service.list[0].model).toBe('308');
  });

  it('removes a vehicle when toggled a second time', () => {
    service.toggle(listing(1));
    service.toggle(listing(1));

    expect(service.count).toBe(0);
    expect(service.isFavorite(1)).toBeFalse();
  });

  it('keeps the most recently saved vehicle first', () => {
    service.toggle(listing(1));
    service.toggle(listing(2));

    expect(service.list.map((v) => v.id)).toEqual([2, 1]);
  });

  it('does not restore favorites across service instances (session-only)', () => {
    service.toggle(listing(7));

    // A brand-new instance (e.g. a page reload) must boot with no favorites.
    const reloaded = new FavoritesService();
    expect(reloaded.count).toBe(0);
    expect(reloaded.isFavorite(7)).toBeFalse();
  });

  it('clears all favorites', () => {
    service.toggle(listing(1));
    service.toggle(listing(2));

    service.clear();

    expect(service.count).toBe(0);
    expect(service.isFavorite(1)).toBeFalse();
  });
});