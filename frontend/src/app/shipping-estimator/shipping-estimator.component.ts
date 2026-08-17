import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShippingService } from '../services/shipping.service';
import { ShippingSelectionService } from '../services/shipping-selection.service';
import { TranslationService } from '../services/translation.service';
import { TranslatePipe } from '../pipes/translate.pipe';
import { DestinationPort, OriginPort, ShippingEstimateResponse } from '../models/shipping.model';

@Component({
  selector: 'app-shipping-estimator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './shipping-estimator.component.html',
  styleUrls: ['./shipping-estimator.component.css']
})
export class ShippingEstimatorComponent {
  originPorts: OriginPort[] = ['MARSEILLE', 'ALICANTE', 'SETE'];
  destinationPorts: DestinationPort[] = ['ALGER', 'ORAN', 'BEJAIA'];

  result: ShippingEstimateResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private shippingService: ShippingService,
    private translationService: TranslationService,
    // public so the template can bind [(ngModel)] directly to the shared signals
    public selection: ShippingSelectionService
  ) {}

  get originPort(): OriginPort {
    return this.selection.originPort();
  }

  set originPort(value: OriginPort) {
    this.selection.originPort.set(value);
  }

  get destinationPort(): DestinationPort {
    return this.selection.destinationPort();
  }

  set destinationPort(value: DestinationPort) {
    this.selection.destinationPort.set(value);
  }

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
        this.errorMessage = err.error?.message || this.translationService.t('errors.shippingEstimate');
        this.loading = false;
      }
    });
  }
}
