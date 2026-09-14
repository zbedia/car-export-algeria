import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { VehicleService } from './vehicle.service';

describe('VehicleService', () => {
  let service: VehicleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(VehicleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends maxPrice 0 so it filters instead of being treated as "no limit"', () => {
    service.search({ maxPrice: 0 }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/vehicles/search'));
    expect(req.request.params.get('maxPrice')).toBe('0');
    req.flush([]);
  });

  it('omits maxPrice when it is null', () => {
    service.search({ maxPrice: null }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/vehicles/search'));
    expect(req.request.params.get('maxPrice')).toBeNull();
    req.flush([]);
  });

  it('omits maxMileageKm when it is null and includes it when 0', () => {
    service.search({ maxPrice: 5000, maxMileageKm: null }).subscribe();

    let req = httpMock.expectOne((r) => r.url.endsWith('/api/vehicles/search'));
    expect(req.request.params.get('maxMileageKm')).toBeNull();
    req.flush([]);

    service.search({ maxPrice: 5000, maxMileageKm: 0 }).subscribe();

    req = httpMock.expectOne((r) => r.url.endsWith('/api/vehicles/search'));
    expect(req.request.params.get('maxMileageKm')).toBe('0');
    req.flush([]);
  });

  it('appends all fuel types and ignores empty ones', () => {
    service.search({ fuelTypes: ['ESSENCE', 'HYBRIDE'], maxPrice: 10000 }).subscribe();

    const req = httpMock.expectOne((r) => r.url.endsWith('/api/vehicles/search'));
    expect(req.request.params.getAll('fuelType')).toEqual(['ESSENCE', 'HYBRIDE']);
    req.flush([]);
  });
});