import { AfterViewInit, Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../pipes/translate.pipe';
import { PortSelectorComponent } from '../port-selector/port-selector.component';
import { DestinationPort, OriginPort } from '../models/shipping.model';

export interface ShippingEditResult {
  originPort: OriginPort;
  destinationPort: DestinationPort;
}

@Component({
  selector: 'app-shipping-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, PortSelectorComponent],
  templateUrl: './shipping-edit-modal.component.html',
  styleUrls: ['./shipping-edit-modal.component.css']
})
export class ShippingEditModalComponent implements AfterViewInit {
  @Input() originPort: OriginPort = 'MARSEILLE';
  @Input() destinationPort: DestinationPort = 'ALGER';
  @Output() save = new EventEmitter<ShippingEditResult>();
  @Output() cancel = new EventEmitter<void>();

  @ViewChild('cancelButton') private cancelButton?: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    this.cancelButton?.nativeElement.focus();
  }

  // Escape closes the dialog the same way the backdrop click and Cancel do.
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.onCancel();
  }

  onSave(): void {
    this.save.emit({ originPort: this.originPort, destinationPort: this.destinationPort });
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
