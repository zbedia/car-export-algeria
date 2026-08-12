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
}
