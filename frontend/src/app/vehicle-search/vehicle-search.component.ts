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

  // Per-vehicle shipping estimate state, keyed by vehicle id.
  private shippingExpandedIds = new Set<number>();
  private shippingLoadingIds = new Set<number>();
  private shippingResults = new Map<number, ShippingEstimateResponse>();
  private shippingErrors = new Map<number, string>();

  editingVehicleId: number | null = null;

  constructor(
    private vehicleService: VehicleService,
    private shippingService: ShippingService,
    private shippingSelection: ShippingSelectionService,
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

    this.shippingLoadingIds.add(vehicle.id);
    this.shippingErrors.delete(vehicle.id);

    const origin = this.shippingSelection.originPort();
    const destination = this.shippingSelection.destinationPort();

    this.shippingService.estimate(origin, destination).subscribe({
      next: (result) => {
        this.shippingResults.set(vehicle.id, result);
        this.shippingLoadingIds.delete(vehicle.id);
      },
      error: (err) => {
        this.shippingErrors.set(vehicle.id, err.error?.message || this.translationService.t('errors.shippingEstimate'));
        this.shippingLoadingIds.delete(vehicle.id);
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

  openEditModal(vehicleId: number): void {
    this.editingVehicleId = vehicleId;
  }

  onModalCancel(): void {
    this.editingVehicleId = null;
  }

  onModalSave(result: ShippingEditResult): void {
    const vehicleId = this.editingVehicleId;
    if (vehicleId === null) {
      return;
    }

    this.shippingLoadingIds.add(vehicleId);
    this.shippingErrors.delete(vehicleId);

    this.shippingService.estimate(result.originPort, result.destinationPort).subscribe({
      next: (data) => {
        this.shippingResults.set(vehicleId, data);
        this.shippingLoadingIds.delete(vehicleId);
        this.editingVehicleId = null;
      },
      error: (err) => {
        this.shippingErrors.set(vehicleId, err.error?.message || this.translationService.t('errors.shippingEstimate'));
        this.shippingLoadingIds.delete(vehicleId);
        this.editingVehicleId = null;
      }
    });
  }

  currentOriginForVehicle(vehicleId: number): OriginPort {
    return this.shippingResults.get(vehicleId)?.originPort ?? this.shippingSelection.originPort();
  }

  currentDestinationForVehicle(vehicleId: number): DestinationPort {
    return this.shippingResults.get(vehicleId)?.destinationPort ?? this.shippingSelection.destinationPort();
  }
}
