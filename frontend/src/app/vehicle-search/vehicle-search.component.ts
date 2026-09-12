import { Component, ElementRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService, VehicleSearchFilters } from '../services/vehicle.service';
import { ShippingService } from '../services/shipping.service';
import { ShippingSelectionService } from '../services/shipping-selection.service';
import { WhatsAppService } from '../services/whatsapp.service';
import { TranslationService } from '../services/translation.service';
import { TranslatePipe } from '../pipes/translate.pipe';
import { ShippingEditModalComponent, ShippingEditResult } from '../shipping-edit-modal/shipping-edit-modal.component';
import { FuelType, VehicleSearchResult } from '../models/vehicle-search-result.model';
import { ShippingEstimateResponse } from '../models/shipping.model';
import { BRAND_LOGO_SLUGS, CAR_BRANDS } from '../data/car-brands';
import { ALL_MODELS, CAR_MODELS_BY_BRAND } from '../data/car-models';

interface VehicleGroup {
  brand: string;
  model: string;
  vehicles: VehicleSearchResult[];
  cheapestSource: string | null;
}

// Diesel is always filtered out server-side, so it can never be rendered:
// a "no results" hint would be the only outcome of offering it as a filter.
const SELECTABLE_FUEL_TYPES = ['ESSENCE', 'HYBRIDE', 'ELECTRIQUE'] as const;

const FUEL_TYPE_ICONS: Record<(typeof SELECTABLE_FUEL_TYPES)[number], string> = {
  ESSENCE: '⛽',
  HYBRIDE: '🔋',
  ELECTRIQUE: '⚡'
};

// Quick-search badges under the search bar: each one combines filters
// directly supported by the search API (max price, a list of fuel types,
// or an exact listing source) so a single click kicks off a real search.
const QUICK_MAX_PRICE_EUR = 15_000;
const QUICK_ECO_FUEL_TYPES: readonly FuelType[] = ['ELECTRIQUE', 'HYBRIDE'];
const QUICK_SOURCE_FRANCE = 'AutoExportMarseille';
const QUICK_SOURCE_SWEDEN = 'CarXExport';
const FLAG_FRANCE_URL = 'https://flagcdn.com/fr.svg';
const FLAG_SWEDEN_URL = 'https://flagcdn.com/se.svg';

interface QuickBadge {
  key: string;
  icon: string;
  flagUrl: string | null;
  active: boolean;
  apply: () => void;
}

const TOTAL_ICON = '🚗';

// Comparison supports up to 3 vehicles side by side; the toolbar disables
// further picks once the limit is reached.
const MAX_COMPARED_VEHICLES = 3;

function isSelectableFuelType(fuelType: FuelType): fuelType is (typeof SELECTABLE_FUEL_TYPES)[number] {
  return (SELECTABLE_FUEL_TYPES as readonly FuelType[]).includes(fuelType);
}

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
  // Bound to <input type="number"> via [(ngModel)], which sets null when the
  // field is cleared — so the property type mirrors the runtime reality.
  maxPrice: number | null = null;
  maxMileageKm: number | null = null;
  garageCity = '';
  fuelType: FuelType | '' = '';
  // Filters applied by the quick-search badges (not bound to the form):
  // the combined fuel badge drives both fuel types at once, and the
  // country badges restrict by listing source.
  quickFuelTypes: FuelType[] | null = null;
  quickSource: string | null = null;
  groupedResults: VehicleGroup[] = [];
  loading = false;
  errorMessage = '';
  hasSearched = false;

  // Autocomplete dropdowns (brand/model) and the collapsible advanced filters.
  brandDropdownOpen = false;
  modelDropdownOpen = false;
  advancedOpen = false;
  private brokenBrandLogos = new Set<string>();

  // Summary strip: one listing counts once, regardless of how models are
  // grouped on screen. Diesel can never reach the client (server-side filter),
  // so the per-fuel counts below only cover the three selectable fuel types.
  totalCount = 0;
  private fuelCounts: Record<(typeof SELECTABLE_FUEL_TYPES)[number], number> = {
    ESSENCE: 0,
    HYBRIDE: 0,
    ELECTRIQUE: 0
  };

  pageSize = 10;
  currentPage = 1;

  carBrands = CAR_BRANDS;

  private brokenImageIds = new Set<number>();

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    const vehicleId = Number(img.dataset['vehicleId']);
    if (Number.isFinite(vehicleId)) {
      this.brokenImageIds.add(vehicleId);
    }
  }

  isImageBroken(vehicleId: number): boolean {
    return this.brokenImageIds.has(vehicleId);
  }

  formatPrice(price: number, currency: string): string {
    return this.formatNumber(price, {
      style: 'currency',
      currency,
      maximumFractionDigits: 0
    });
  }

  formatMileage(mileageKm: number): string {
    return this.formatNumber(mileageKm, { maximumFractionDigits: 0 });
  }

  private formatNumber(value: number, options: Intl.NumberFormatOptions): string {
    return new Intl.NumberFormat(this.translationService.locale, options).format(value);
  }

  // Diesel is intentionally excluded — it's always filtered out server-side
  // (banned for private import), so offering it as a filter option would
  // just lead to a confusing "no results" every time.
  selectableFuelTypes: readonly FuelType[] = [...SELECTABLE_FUEL_TYPES];

  totalIcon = TOTAL_ICON;

  // Recomputed from every search response (the flat listing array), NOT from
  // the grouped-and-paginated view: a total must never shrink to the current
  // page size.
  private updateStats(results: VehicleSearchResult[]): void {
    this.totalCount = results.length;
    this.fuelCounts = { ESSENCE: 0, HYBRIDE: 0, ELECTRIQUE: 0 };
    for (const v of results) {
      if (isSelectableFuelType(v.fuelType)) {
        this.fuelCounts[v.fuelType]++;
      }
    }
  }

  fuelCount(fuelType: FuelType): number {
    return isSelectableFuelType(fuelType) ? this.fuelCounts[fuelType] : 0;
  }

  formatCount(value: number): string {
    return this.formatNumber(value, { maximumFractionDigits: 0 });
  }

  // Shared sea-freight estimate for the route selected in
  // ShippingSelectionService. Freight depends only on origin/destination,
  // so a single estimate backs the cost breakdown on every vehicle card;
  // changing the route re-fetches it once for all cards.
  routeShipping: ShippingEstimateResponse | null = null;
  routeShippingError = '';

editingShipping = false;

  // --- Side-by-side comparison (up to 3 vehicles) ---
  selectedVehicles: VehicleSearchResult[] = [];
  compareOpen = false;

  get compareLimit(): number {
    return MAX_COMPARED_VEHICLES;
  }

  isVehicleSelected(vehicle: VehicleSearchResult): boolean {
    return this.selectedVehicles.some((v) => v.id === vehicle.id);
  }

  toggleCompare(vehicle: VehicleSearchResult): void {
    const index = this.selectedVehicles.findIndex((v) => v.id === vehicle.id);
    if (index >= 0) {
      this.selectedVehicles.splice(index, 1);
    } else if (this.selectedVehicles.length < MAX_COMPARED_VEHICLES) {
      this.selectedVehicles.push(vehicle);
    }
  }

  removeFromCompare(index: number): void {
    this.selectedVehicles.splice(index, 1);
  }

  openCompare(): void {
    this.compareOpen = true;
  }

  closeCompare(): void {
    this.compareOpen = false;
  }

  clearComparison(): void {
    this.selectedVehicles = [];
    this.compareOpen = false;
  }

  // Index of the column with the lowest delivered price (highlighted as the
  // best deal). Null while fewer than 2 comparable totals are available.
  get bestCompareIndex(): number | null {
    const totals = this.selectedVehicles.map((v) => this.totalDeliveredFor(v));
    let best: number | null = null;
    for (let i = 0; i < totals.length; i++) {
      if (totals[i] === null) continue;
      if (best === null || (totals[i] as number) < (totals[best] as number)) {
        best = i;
      }
    }
    return best !== null && this.selectedVehicles.length > 1 ? best : null;
  }

  // Eligibility line rendered in the comparison table header (age-based when
  // the server provides the vehicle age, Finance Law compliance otherwise).
  compareEligibilityText(vehicle: VehicleSearchResult): string {
    return vehicle.ageMonths !== null
      ? `${this.translationService.t('eligibility.eligible')} — ${this.formatAge(vehicle.ageMonths)}`
      : this.translationService.t('eligibility.conformsLaw');
  }

  constructor(
    private vehicleService: VehicleService,
    private shippingService: ShippingService,
    public shippingSelection: ShippingSelectionService,
    private whatsapp: WhatsAppService,
    private translationService: TranslationService,
    private el: ElementRef
  ) {
    this.loadRouteShipping();
  }

  // Remembers the listing the user most recently opened, so the floating
  // WhatsApp button can pre-fill its message with that vehicle's reference.
  consultVehicle(vehicle: VehicleSearchResult): void {
    this.whatsapp.consult(`${vehicle.brand} ${vehicle.model} ${vehicle.year} (ref. #${vehicle.id})`);
  }

  private loadRouteShipping(): void {
    this.routeShippingError = '';
    this.shippingService
      .estimate(this.shippingSelection.originPort(), this.shippingSelection.destinationPort())
      .subscribe({
        next: (result) => {
          this.routeShipping = result;
          this.routeShippingError = '';
        },
        error: () => {
          this.routeShipping = null;
          this.routeShippingError = this.translationService.t('errors.shippingEstimate');
        }
      });
  }

  onSearch(): void {
    // Enter in a form field triggers ngSubmit even when the submit button
    // is disabled — guard against out-of-order parallel searches.
    if (this.loading) return;

    // Drop shipping state from any previous search: stale estimates and
    // in-flight requests would otherwise accumulate forever and leak
    // across unrelated result sets. The route freight itself is shared
    // and stays cached in routeShipping.
    this.brokenImageIds.clear();

    this.loading = true;
    this.errorMessage = '';
    this.hasSearched = true;

    const filters: VehicleSearchFilters = {
      brand: this.brand,
      model: this.model,
      maxPrice: this.maxPrice,
      maxMileageKm: this.maxMileageKm,
      garageCity: this.garageCity,
      fuelTypes: this.selectedFuelTypes,
      source: this.quickSource ?? undefined
    };

    this.vehicleService.search(filters).subscribe({
      next: (data) => {
        this.groupedResults = this.groupByModel(data);
        this.updateStats(data);
        this.currentPage = 1;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || this.translationService.t('errors.generic');
        this.loading = false;
      }
    });
  }

  // Clears every filter, wipes the results and returns the form to its
  // pristine state (used by the "clear filters" button).
  clearFilters(): void {
    this.brand = '';
    this.model = '';
    this.maxPrice = null;
    this.maxMileageKm = null;
    this.garageCity = '';
    this.fuelType = '';
    this.quickFuelTypes = null;
    this.quickSource = null;

    this.brandDropdownOpen = false;
    this.modelDropdownOpen = false;
    this.groupedResults = [];
    this.currentPage = 1;
    this.hasSearched = false;
    this.errorMessage = '';
    this.totalCount = 0;
    this.fuelCounts = { ESSENCE: 0, HYBRIDE: 0, ELECTRIQUE: 0 };

    this.brokenImageIds.clear();
  }

  // --- Quick-search badges ---
  // A badge maps to a single click that runs a real search. When reached,
  // its "apply" sets the underlying filter directly on the form state
  // (max price) or in the badge-only state (fuel/source), then searches.
  get quickBadges(): QuickBadge[] {
    return [
      {
        key: 'search.quickUnder15000',
        icon: '💶',
        flagUrl: null,
        active: this.maxPrice === QUICK_MAX_PRICE_EUR,
        apply: () => {
          this.maxPrice = QUICK_MAX_PRICE_EUR;
          this.onSearch();
        }
      },
      {
        key: 'search.quickElectricHybrid',
        icon: '🔋',
        flagUrl: null,
        active: this.quickFuelTypes !== null && this.sameFuelTypes(this.quickFuelTypes, QUICK_ECO_FUEL_TYPES),
        apply: () => {
          this.quickFuelTypes = this.quickFuelTypes !== null ? null : [...QUICK_ECO_FUEL_TYPES];
          this.onSearch();
        }
      },
      {
        key: 'search.quickFrance',
        icon: '',
        flagUrl: FLAG_FRANCE_URL,
        active: this.quickSource === QUICK_SOURCE_FRANCE,
        apply: () => {
          this.quickSource = this.quickSource === QUICK_SOURCE_FRANCE ? null : QUICK_SOURCE_FRANCE;
          this.onSearch();
        }
      },
      {
        key: 'search.quickSweden',
        icon: '',
        flagUrl: FLAG_SWEDEN_URL,
        active: this.quickSource === QUICK_SOURCE_SWEDEN,
        apply: () => {
          this.quickSource = this.quickSource === QUICK_SOURCE_SWEDEN ? null : QUICK_SOURCE_SWEDEN;
          this.onSearch();
        }
      }
    ];
  }

  // The badge-driven fuel list takes precedence; otherwise the single
  // fuel <select> supplies a one-element list. Diesel can never appear:
  // it is filtered out server-side.
  get selectedFuelTypes(): FuelType[] | undefined {
    if (this.quickFuelTypes) {
      return this.quickFuelTypes;
    }
    return this.fuelType ? [this.fuelType] : undefined;
  }

  // Manually choosing a fuel type in the <select> overrides the "Electric /
  // Hybrid" badge, so the badge stops claiming to be the active filter.
  onFuelTypeChange(): void {
    this.quickFuelTypes = null;
  }

  private sameFuelTypes(a: FuelType[], b: readonly FuelType[]): boolean {
    return a.length === b.length && b.every((f) => a.includes(f));
  }

  // --- Pagination (client-side over the model groups) ---
  get pagedGroups(): VehicleGroup[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.groupedResults.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.groupedResults.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.goToPage(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.goToPage(this.currentPage + 1);
    }
  }

  goToPage(page: number): void {
    this.currentPage = Math.min(Math.max(1, page), this.totalPages);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private groupByModel(results: VehicleSearchResult[]): VehicleGroup[] {
    const groups = new Map<string, VehicleGroup>();
    for (const v of results) {
      const key = `${v.brand}|${v.model}`;
      if (!groups.has(key)) {
        groups.set(key, { brand: v.brand, model: v.model, vehicles: [], cheapestSource: null });
      }
      groups.get(key)!.vehicles.push(v);
    }
    for (const group of groups.values()) {
      group.vehicles.sort((a, b) => a.price - b.price);
      group.cheapestSource = group.vehicles.find((v) => v.bestPrice)?.cheapestSource ?? null;
    }
    return Array.from(groups.values());
  }

  fuelIcon(fuelType: FuelType): string {
    return isSelectableFuelType(fuelType) ? FUEL_TYPE_ICONS[fuelType] : '';
  }

  get modelSuggestions(): string[] {
    return CAR_MODELS_BY_BRAND[this.brand] ?? ALL_MODELS;
  }

  get brandSuggestions(): string[] {
    const q = this.brand.trim().toLowerCase();
    if (!q) return [];
    return this.carBrands.filter((b) => b.toLowerCase().includes(q)).slice(0, 8);
  }

  get filteredModels(): string[] {
    const q = this.model.trim().toLowerCase();
    const all = CAR_MODELS_BY_BRAND[this.brand] ?? ALL_MODELS;
    if (!q) return all.slice(0, 8);
    return all.filter((m) => m.toLowerCase().includes(q)).slice(0, 8);
  }

  selectBrand(brand: string): void {
    this.onBrandChange(brand);
    this.brandDropdownOpen = false;
  }

  selectModel(model: string): void {
    this.model = model;
    this.modelDropdownOpen = false;
  }

  brandLogoUrl(brand: string): string {
    const slug = BRAND_LOGO_SLUGS[brand];
    return slug ? `https://cdn.simpleicons.org/${slug}` : '';
  }

  logoFailed(brand: string): boolean {
    return this.brokenBrandLogos.has(brand);
  }

  markLogoFailed(brand: string): void {
    if (BRAND_LOGO_SLUGS[brand]) {
      this.brokenBrandLogos.add(brand);
    }
  }

  @HostListener('document:click', ['$event'])
  private closeDropdownsOnOutsideClick(event: MouseEvent): void {
    const clickedInside = this.el.nativeElement.contains(event.target);
    if (!clickedInside) {
      this.brandDropdownOpen = false;
      this.modelDropdownOpen = false;
    }
  }

  onBrandChange(value: string): void {
    this.brand = value;

    // If the currently typed model doesn't belong to the newly selected
    // brand's known models, clear it — otherwise a leftover model like
    // "308" would silently stay selected after switching to Renault.
    // Left untouched when the brand isn't recognized (free text), since
    // we have no model list to validate against in that case.
    const knownModels = CAR_MODELS_BY_BRAND[this.brand];
    if (knownModels && !knownModels.includes(this.model)) {
      this.model = '';
    }
  }

  customsDiscountReasonText(vehicle: VehicleSearchResult): string {
    if (
      vehicle.customsDiscountReasonCode === 'ELECTRIC' ||
      vehicle.customsDiscountReasonCode === 'DIESEL_NOT_ELIGIBLE'
    ) {
      return this.translationService.t(`discountReason.${vehicle.customsDiscountReasonCode}`);
    }

    const fuelLabel = this.translationService.t(`fuel.${vehicle.fuelType}`);
    return this.translationService.t(`discountReason.${vehicle.customsDiscountReasonCode}`, {
      fuel: fuelLabel,
      threshold: vehicle.engineDisplacementThresholdCm3 ?? '',
      displacement: vehicle.engineDisplacementCm3 ?? ''
    });
  }

  // --- Cost breakdown on each card ---

  formatDzd(value: number): string {
    return `${this.formatNumber(value, { maximumFractionDigits: 0 })} DZD`;
  }

  // Total delivered in Algeria: purchase price + customs (EUR) + sea freight.
  // Null while the components aren't available (no customs data or freight).
  totalDeliveredFor(vehicle: VehicleSearchResult): number | null {
    if (!this.routeShipping || !vehicle.customs) {
      return null;
    }
    return vehicle.price + vehicle.customs.totalEur + this.routeShipping.totalCost;
  }

  // Age is stored server-side as whole months; "Éligible — 2 ans et 4 mois".
  formatAge(ageMonths: number): string {
    const years = Math.floor(ageMonths / 12);
    const months = ageMonths % 12;
    return this.translationService.t('eligibility.age', { years, months });
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

    // A single freight estimate backs every card; refresh it in bulk.
    this.loadRouteShipping();

    this.editingShipping = false;
  }
}
