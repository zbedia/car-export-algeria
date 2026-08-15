import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DestinationPort, OriginPort, ShippingEstimateResponse } from '../models/shipping.model';

@Injectable({ providedIn: 'root' })
export class ShippingService {
  private apiUrl = `${environment.apiUrl}/shipping`;

  constructor(private http: HttpClient) {}

  estimate(originPort: OriginPort, destinationPort: DestinationPort): Observable<ShippingEstimateResponse> {
    const params = new HttpParams()
      .set('originPort', originPort)
      .set('destinationPort', destinationPort);

    return this.http.get<ShippingEstimateResponse>(`${this.apiUrl}/estimate`, { params });
  }
}
