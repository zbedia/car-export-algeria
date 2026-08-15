import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShippingService } from '../services/shipping.service';
import { DestinationPort, OriginPort, ShippingEstimateResponse } from '../models/shipping.model';

@Component({
  selector: 'app-shipping-estimator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './shipping-estimator.component.html',
  styleUrls: ['./shipping-estimator.component.css']
})
export class ShippingEstimatorComponent {
  originPorts: OriginPort[] = ['MARSEILLE', 'ALICANTE', 'SETE'];
  destinationPorts: DestinationPort[] = ['ALGER', 'ORAN', 'BEJAIA'];

  originPort: OriginPort = 'MARSEILLE';
  destinationPort: DestinationPort = 'ALGER';

  result: ShippingEstimateResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(private shippingService: ShippingService) {}

  onEstimate(): void {
    this.loading = true;
    this.errorMessage = '';
    this.result = null;

    this.shippingService.estimate(this.originPort, this.destinationPort).subscribe({
      next: (data) => {
        this.result = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Could not estimate shipping cost.';
        this.loading = false;
      }
    });
  }
}
