import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../pipes/translate.pipe';
import { DestinationPort, OriginPort } from '../models/shipping.model';

export interface ShippingEditResult {
  originPort: OriginPort;
  destinationPort: DestinationPort;
}

@Component({
  selector: 'app-shipping-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './shipping-edit-modal.component.html',
  styleUrls: ['./shipping-edit-modal.component.css']
})
export class ShippingEditModalComponent {
  @Input() originPort: OriginPort = 'MARSEILLE';
  @Input() destinationPort: DestinationPort = 'ALGER';
  @Output() save = new EventEmitter<ShippingEditResult>();
  @Output() cancel = new EventEmitter<void>();

  originPorts: OriginPort[] = ['MARSEILLE', 'ALICANTE', 'SETE'];
  destinationPorts: DestinationPort[] = ['ALGER', 'ORAN', 'BEJAIA'];

  onSave(): void {
    this.save.emit({ originPort: this.originPort, destinationPort: this.destinationPort });
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
