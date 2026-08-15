import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService } from '../services/vehicle.service';
import { TranslationService } from '../services/translation.service';
import { TranslatePipe } from '../pipes/translate.pipe';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';

interface VehicleGroup {
  brand: string;
  model: string;
  vehicles: VehicleSearchResult[];
}

const FUEL_TYPE_ICONS: Record<FuelType, string> = {
  ESSENCE: '⛽',
  HYBRIDE: '🔋',
  ELECTRIQUE: '⚡',
  DIESEL: '🚫'
};

@Component({
  selector: 'app-vehicle-search',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './vehicle-search.component.html',
  styleUrls: ['./vehicle-search.component.css']
})
export class VehicleSearchComponent {
  brand = '';
  model = '';
  maxPrice?: number;
  groupedResults: VehicleGroup[] = [];
  loading = false;
  errorMessage = '';
  hasSearched = false;

  constructor(
    private vehicleService: VehicleService,
    private translationService: TranslationService
  ) {}

  onSearch(): void {
    this.loading = true;
    this.errorMessage = '';
    this.hasSearched = true;

    this.vehicleService.search(this.brand, this.model, this.maxPrice).subscribe({
      next: (data) => {
        this.groupedResults = this.groupByModel(data);
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || this.translationService.t('errors.generic');
        this.loading = false;
      }
    });
  }

  private groupByModel(results: VehicleSearchResult[]): VehicleGroup[] {
    const groups = new Map<string, VehicleGroup>();
    for (const v of results) {
      const key = `${v.brand}|${v.model}`;
      if (!groups.has(key)) {
        groups.set(key, { brand: v.brand, model: v.model, vehicles: [] });
      }
      groups.get(key)!.vehicles.push(v);
    }
    for (const group of groups.values()) {
      group.vehicles.sort((a, b) => a.price - b.price);
    }
    return Array.from(groups.values());
  }

  fuelIcon(fuelType: FuelType): string {
    return FUEL_TYPE_ICONS[fuelType] ?? '';
  }

  customsDiscountReasonText(vehicle: VehicleSearchResult): string {
    if (vehicle.customsDiscountReasonCode === 'ELECTRIC' || vehicle.customsDiscountReasonCode === 'DIESEL_NOT_ELIGIBLE') {
      return this.translationService.t(`discountReason.${vehicle.customsDiscountReasonCode}`);
    }

    const fuelLabel = this.translationService.t(`fuel.${vehicle.fuelType}`);
    return this.translationService.t(`discountReason.${vehicle.customsDiscountReasonCode}`, {
      fuel: fuelLabel,
      threshold: vehicle.engineDisplacementThresholdCm3 ?? '',
      displacement: vehicle.engineDisplacementCm3 ?? ''
    });
  }
}
