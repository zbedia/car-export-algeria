import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService } from '../services/vehicle.service';
import { ShippingService } from '../services/shipping.service';
import { ShippingSelectionService } from '../services/shipping-selection.service';
import { TranslationService } from '../services/translation.service';
import { TranslatePipe } from '../pipes/translate.pipe';
import { ShippingEditModalComponent, ShippingEditResult } from '../shipping-edit-modal/shipping-edit-modal.component';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';
import { DestinationPort, OriginPort, ShippingEstimateResponse } from '../models/shipping.model';
import { CAR_BRANDS } from '../data/car-brands';
import { ALL_MODELS, CAR_MODELS_BY_BRAND } from '../data/car-models';

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
  imports: [CommonModule, FormsModule, TranslatePipe, ShippingEditModalComponent],
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

  carBrands = CAR_BRANDS;

  // Shipping estimate state, keyed by vehicle id. The route itself
  // (origin/destination) is shared across every vehicle via
  // ShippingSelectionService — editing it from any card refreshes all
  // cards that already have a result, instead of each card tracking
  // its own independent route.
  private shippingExpandedIds = new Set<number>();
  private shippingLoadingIds = new Set<number>();
  private shippingResults = new Map<number, ShippingEstimateResponse>();
  private shippingErrors = new Map<number, string>();

  editingShipping = false;

  constructor(
    private vehicleService: VehicleService,
    private shippingService: ShippingService,
    public shippingSelection: ShippingSelectionService,
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

  get modelSuggestions(): string[] {
    return CAR_MODELS_BY_BRAND[this.brand] ?? ALL_MODELS;
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

  toggleShippingEstimate(vehicle: VehicleSearchResult): void {
    if (this.shippingExpandedIds.has(vehicle.id)) {
      this.shippingExpandedIds.delete(vehicle.id);
      return;
    }

    this.shippingExpandedIds.add(vehicle.id);

    // Already fetched (or currently fetching) — just show what we have.
    if (this.shippingResults.has(vehicle.id) || this.shippingLoadingIds.has(vehicle.id)) {
      return;
    }

    this.fetchShippingEstimate(
      vehicle.id,
      this.shippingSelection.originPort(),
      this.shippingSelection.destinationPort()
    );
  }

  private fetchShippingEstimate(vehicleId: number, origin: OriginPort, destination: DestinationPort): void {
    this.shippingLoadingIds.add(vehicleId);
    this.shippingErrors.delete(vehicleId);

    this.shippingService.estimate(origin, destination).subscribe({
      next: (result) => {
        this.shippingResults.set(vehicleId, result);
        this.shippingLoadingIds.delete(vehicleId);
      },
      error: (err) => {
        this.shippingErrors.set(vehicleId, err.error?.message || this.translationService.t('errors.shippingEstimate'));
        this.shippingLoadingIds.delete(vehicleId);
      }
    });
  }

  isShippingExpanded(vehicleId: number): boolean {
    return this.shippingExpandedIds.has(vehicleId);
  }

  isShippingLoading(vehicleId: number): boolean {
    return this.shippingLoadingIds.has(vehicleId);
  }

  shippingResultFor(vehicleId: number): ShippingEstimateResponse | undefined {
    return this.shippingResults.get(vehicleId);
  }

  shippingErrorFor(vehicleId: number): string | undefined {
    return this.shippingErrors.get(vehicleId);
  }

  openEditModal(): void {
    this.editingShipping = true;
  }

  onModalCancel(): void {
    this.editingShipping = false;
  }

  onModalSave(result: ShippingEditResult): void {
    this.shippingSelection.originPort.set(result.originPort);
    this.shippingSelection.destinationPort.set(result.destinationPort);

    // Refresh every card that already has a visible estimate so they
    // all reflect the new route immediately, without the user needing
    // to re-open each one individually.
    for (const vehicleId of this.shippingResults.keys()) {
      this.fetchShippingEstimate(vehicleId, result.originPort, result.destinationPort);
    }

    this.editingShipping = false;
  }
}
