import { Injectable } from '@angular/core';
import { VehicleSearchResult } from '../models/vehicle-search-result.model';

// Session-only favorites: the search is public (no account) and the app must
// boot in a pristine state — no heart pre-selected, no favorites bar — so the
// saved listings live in memory for the current session only and never
// survive a page reload.
@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private favorites: VehicleSearchResult[] = [];

  get list(): VehicleSearchResult[] {
    return this.favorites;
  }

  get count(): number {
    return this.favorites.length;
  }

  isFavorite(id: number): boolean {
    return this.favorites.some((v) => v.id === id);
  }

  toggle(vehicle: VehicleSearchResult): void {
    if (this.isFavorite(vehicle.id)) {
      this.remove(vehicle.id);
    } else {
      this.favorites = [vehicle, ...this.favorites];
    }
  }

  remove(id: number): void {
    this.favorites = this.favorites.filter((v) => v.id !== id);
  }

  clear(): void {
    this.favorites = [];
  }
}