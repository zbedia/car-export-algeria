import { TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { VehicleSearchComponent } from './vehicle-search.component';
import { VehicleService } from '../services/vehicle.service';
import { ShippingService } from '../services/shipping.service';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';
import { ShippingEstimateResponse } from '../models/shipping.model';

const routeStub: ShippingEstimateResponse = {
  originPort: 'MARSEILLE',
  destinationPort: 'ALGER',
  baseFreightCost: 850,
  handlingFee: 150,
  totalCost: 1000,
  currency: 'EUR'
};

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

describe('VehicleSearchComponent', () => {
  let component: VehicleSearchComponent;
  let searchSpy: jasmine.Spy;

  beforeEach(() => {
    searchSpy = jasmine.createSpy('search');
    TestBed.configureTestingModule({
      imports: [VehicleSearchComponent],
      providers: [
        { provide: VehicleService, useValue: { search: searchSpy } },
        { provide: ShippingService, useValue: { estimate: jasmine.createSpy('estimate').and.returnValue(of(routeStub)) } }
      ]
    });
    const fixture = TestBed.createComponent(VehicleSearchComponent);
    component = fixture.componentInstance;
  });

  it('groups by brand+model, sorts by price and resolves the cheapest source', () => {
    const expensive = listing(1, { model: '308', price: 16500 });
    const cheap = listing(2, {
      model: '308',
      price: 14800,
      source: 'ExportCar213',
      bestPrice: true,
      cheapestSource: 'ExportCar213'
    });
    const otherModel = listing(3, { model: '3008', price: 21900, bestPrice: true });
    searchSpy.and.returnValue(of([expensive, otherModel, cheap]));

    component.onSearch();

    expect(component.groupedResults.length).toBe(2);
    const group = component.groupedResults.find((g) => g.model === '308');
    expect(group?.vehicles.map((v) => v.id)).toEqual([2, 1]);
    expect(group?.cheapestSource).toBe('ExportCar213');
  });

  it('ignores further searches while one is still loading', () => {
    searchSpy.and.returnValue(new Observable<void>(() => {}));
    component.onSearch();
    component.onSearch();
    expect(searchSpy).toHaveBeenCalledTimes(1);
  });

  it('shows a progress state and clears errors on a new search', () => {
    component.errorMessage = 'stale error';
    searchSpy.and.returnValue(of([]));

    component.onSearch();

    expect(component.errorMessage).toBe('');
    expect(component.hasSearched).toBeTrue();
    expect(component.groupedResults).toEqual([]);
  });

  it('paginates the grouped results client-side', () => {
    component.pageSize = 10;
    const many = Array.from({ length: 25 }, (_, i) => listing(i + 1, { brand: `B${i}`, model: `M${i}` }));
    searchSpy.and.returnValue(of(many));

    component.onSearch();

    expect(component.totalPages).toBe(3);
    expect(component.pagedGroups.length).toBe(10);

    component.goToPage(3);
    expect(component.pagedGroups.length).toBe(5);
  });

  it('records a broken image per vehicle id', () => {
    const event = { target: { dataset: { vehicleId: '7' } } } as unknown as Event;

    component.onImageError(event);

    expect(component.isImageBroken(7)).toBeTrue();
    expect(component.isImageBroken(8)).toBeFalse();
  });

  it('computes total and per-fuel counts in the stats strip', () => {
    const data = [
      listing(1, { fuelType: 'ESSENCE' }),
      listing(2, { fuelType: 'HYBRIDE' }),
      listing(3, { fuelType: 'ELECTRIQUE' }),
      listing(4, { fuelType: 'ESSENCE', model: '3008' })
    ];
    searchSpy.and.returnValue(of(data));

    component.onSearch();

    expect(component.totalCount).toBe(4);
    expect(component.fuelCount('ESSENCE')).toBe(2);
    expect(component.fuelCount('HYBRIDE')).toBe(1);
    expect(component.fuelCount('ELECTRIQUE')).toBe(1);
  });

  it('resets counts to zero on an empty result set', () => {
    searchSpy.and.returnValue(of([]));

    component.onSearch();

    expect(component.totalCount).toBe(0);
    expect(component.fuelCount('ESSENCE')).toBe(0);
  });

  it('clears every filter and wipes the results', () => {
    component.brand = 'Peugeot';
    component.model = '308';
    component.maxPrice = 20000;
    component.maxMileageKm = 50000;
    component.garageCity = 'Marseille';
    component.fuelType = 'HYBRIDE';
    component.loading = false;
    searchSpy.and.returnValue(of([listing(1)]));
    component.onSearch();

    component.clearFilters();

    expect(component.brand).toBe('');
    expect(component.model).toBe('');
    expect(component.maxPrice).toBeNull();
    expect(component.maxMileageKm).toBeNull();
    expect(component.garageCity).toBe('');
    expect(component.fuelType).toBe('');
    expect(component.groupedResults).toEqual([]);
    expect(component.hasSearched).toBeFalse();
    expect(component.totalCount).toBe(0);
  });

  it('applies the "under €15,000" badge and searches with that price', () => {
    searchSpy.and.returnValue(of([]));

    const badge = component.quickBadges.find((b) => b.key === 'search.quickUnder15000')!;
    badge.apply();

    expect(component.maxPrice).toBe(15000);
    expect(searchSpy).toHaveBeenCalledWith(jasmine.objectContaining({ maxPrice: 15000 }));
  });

  it('toggles the electric/hybrid badge to send both fuel types', () => {
    searchSpy.and.returnValue(of([]));
    const badge = component.quickBadges.find((b) => b.key === 'search.quickElectricHybrid')!;

    badge.apply();
    expect(searchSpy.calls.mostRecent().args[0].fuelTypes).toEqual(['ELECTRIQUE', 'HYBRIDE']);

    badge.apply();
    expect(searchSpy.calls.mostRecent().args[0].fuelTypes).toBeUndefined();
  });

  it('toggles a country badge to filter by listing source', () => {
    searchSpy.and.returnValue(of([]));
    const france = component.quickBadges.find((b) => b.key === 'search.quickFrance')!;
    const sweden = component.quickBadges.find((b) => b.key === 'search.quickSweden')!;

    france.apply();
    expect(searchSpy.calls.mostRecent().args[0].source).toBe('AutoExportMarseille');

    sweden.apply();
    expect(searchSpy.calls.mostRecent().args[0].source).toBe('CarXExport');

    sweden.apply();
    expect(searchSpy.calls.mostRecent().args[0].source).toBeUndefined();
  });

  it('a country badge wins over the other and manual fuel selection clears the eco badge', () => {
    searchSpy.and.returnValue(of([]));
    component.quickBadges.find((b) => b.key === 'search.quickElectricHybrid')!.apply();
    expect(component.quickFuelTypes).toEqual(['ELECTRIQUE', 'HYBRIDE']);

    component.fuelType = 'ESSENCE';
    component.onFuelTypeChange();
    expect(component.quickFuelTypes).toBeNull();
  });

  it('loads the shared route freight on creation', () => {
    const shipping = TestBed.inject(ShippingService) as unknown as { estimate: jasmine.Spy };
    expect(shipping.estimate).toHaveBeenCalledWith('MARSEILLE', 'ALGER');
    expect(component.routeShipping?.totalCost).toBe(1000);
  });

  it('computes the total delivered price from price + customs + freight', () => {
    const vehicle = listing(1, {
      customs: { dutyEur: 2250, vatEur: 3277.5, totalEur: 5527.5, totalDzd: 1381875, dutyRatePercent: 30 }
    });
    component.routeShipping = routeStub;

    expect(component.totalDeliveredFor(vehicle)).toBe(15000 + 5527.5 + 1000);
    expect(component.totalDeliveredFor(listing(2, { customs: null }))).toBeNull();
    expect(component.totalDeliveredFor(vehicle)).toBeCloseTo(21527.5, 2);
  });

  it('formats age into years and months via the translate service', () => {
    const t = jasmine.createSpy('t').and.returnValue('2 ans et 4 mois');
    (component as unknown as { translationService: { t: jasmine.Spy } }).translationService = { t };

    expect(component.formatAge(28)).toBe('2 ans et 4 mois');
    expect(t).toHaveBeenCalledWith('eligibility.age', { years: 2, months: 4 });
  });
});
