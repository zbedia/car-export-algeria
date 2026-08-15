import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ExchangeRatesResponse } from '../models/currency.model';

@Injectable({ providedIn: 'root' })
export class CurrencyService {
  private apiUrl = `${environment.apiUrl}/currency`;

  constructor(private http: HttpClient) {}

  getRates(): Observable<ExchangeRatesResponse> {
    return this.http.get<ExchangeRatesResponse>(`${this.apiUrl}/rates`);
  }
}
