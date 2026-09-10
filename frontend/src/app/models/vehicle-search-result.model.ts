export type FuelType = 'ESSENCE' | 'HYBRIDE' | 'ELECTRIQUE' | 'DIESEL';
export type CustomsDiscountReasonCode = 'ELECTRIC' | 'DIESEL_NOT_ELIGIBLE' | 'SMALL_ENGINE' | 'LARGE_ENGINE';

export interface CustomsEstimate {
  dutyEur: number;
  vatEur: number;
  totalEur: number;
  totalDzd: number;
  dutyRatePercent: number;
}

export interface VehicleSearchResult {
  id: number;
  source: string;
  externalUrl: string;
  brand: string;
  model: string;
  year: number;
  mileageKm: number;
  price: number;
  currency: string;
  garageCity: string;
  bestPrice: boolean;
  cheapestSource: string | null;
  fuelType: FuelType;
  engineDisplacementCm3: number | null;
  engineDisplacementThresholdCm3: number | null;
  customsDiscountPercentage: number;
  customsDiscountReasonCode: CustomsDiscountReasonCode;
  imageUrl: string | null;
  ageMonths: number | null;
  customs: CustomsEstimate | null;
}
