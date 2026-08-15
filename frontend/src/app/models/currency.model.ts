export type CurrencyCode = 'EUR' | 'DZD';
export type RateType = 'OFFICIAL' | 'PARALLEL';

export interface ExchangeRatesResponse {
  officialRateEurToDzd: number;
  parallelRateEurToDzd: number;
  lastUpdated: string;
}

export interface ConversionResponse {
  originalAmount: number;
  from: CurrencyCode;
  to: CurrencyCode;
  rateType: RateType;
  rateUsed: number;
  convertedAmount: number;
}
