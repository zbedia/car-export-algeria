export type OriginPort = 'MARSEILLE' | 'ALICANTE' | 'SETE';
export type DestinationPort = 'ALGER' | 'ORAN' | 'BEJAIA';

export interface ShippingEstimateResponse {
  originPort: OriginPort;
  destinationPort: DestinationPort;
  baseFreightCost: number;
  handlingFee: number;
  totalCost: number;
  currency: string;
}
