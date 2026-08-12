import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VehicleSearchResult } from '../models/vehicle-search-result.model';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private apiUrl = `${environment.apiUrl}/vehicles`;

  constructor(private http: HttpClient) {}

  search(brand: string, model: string, maxPrice?: number): Observable<VehicleSearchResult[]> {
    let params = new HttpParams();
    if (brand) params = params.set('brand', brand);
    if (model) params = params.set('model', model);
    if (maxPrice) params = params.set('maxPrice', maxPrice.toString());

    return this.http.get<VehicleSearchResult[]>(`${this.apiUrl}/search`, { params });
  }
}
