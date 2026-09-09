import { TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { VehicleSearchComponent } from './vehicle-search.component';
import { VehicleService } from '../services/vehicle.service';
import { ShippingService } from '../services/shipping.service';
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
        { provide: ShippingService, useValue: { estimate: jasmine.createSpy('estimate') } }
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
});
