import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';

export interface VehicleSearchFilters {
  brand?: string;
  model?: string;
  maxPrice?: number;
  maxMileageKm?: number;
  garageCity?: string;
  fuelType?: FuelType | '';
}

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private apiUrl = `${environment.apiUrl}/vehicles`;

  constructor(private http: HttpClient) {}

  search(filters: VehicleSearchFilters): Observable<VehicleSearchResult[]> {
    let params = new HttpParams();
    if (filters.brand) params = params.set('brand', filters.brand);
    if (filters.model) params = params.set('model', filters.model);
    if (filters.maxPrice) params = params.set('maxPrice', filters.maxPrice.toString());
    if (filters.maxMileageKm) params = params.set('maxMileageKm', filters.maxMileageKm.toString());
    if (filters.garageCity) params = params.set('garageCity', filters.garageCity);
    if (filters.fuelType) params = params.set('fuelType', filters.fuelType);

    return this.http.get<VehicleSearchResult[]>(`${this.apiUrl}/search`, { params });
  }
}
