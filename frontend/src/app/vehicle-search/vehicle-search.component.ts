import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleService } from '../services/vehicle.service';
import { VehicleSearchResult } from '../models/vehicle-search-result.model';

interface VehicleGroup {
  brand: string;
  model: string;
  vehicles: VehicleSearchResult[];
}

@Component({
  selector: 'app-vehicle-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  constructor(private vehicleService: VehicleService) {}

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
        this.errorMessage = err.error?.message || 'An error occurred.';
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
}
