import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DestinationPort, OriginPort } from '../models/shipping.model';

/**
 * Shared origin/destination port picker, used by both the standalone
 * RoRo shipping estimator widget and the per-vehicle edit popup — a
 * single place to maintain the list of supported ports.
 */
@Component({
  selector: 'app-port-selector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './port-selector.component.html',
  styleUrls: ['./port-selector.component.css']
})
export class PortSelectorComponent {
  @Input() originPort: OriginPort = 'MARSEILLE';
  @Output() originPortChange = new EventEmitter<OriginPort>();

  @Input() destinationPort: DestinationPort = 'ALGER';
  @Output() destinationPortChange = new EventEmitter<DestinationPort>();

  originPorts: OriginPort[] = ['MARSEILLE', 'ALICANTE', 'SETE'];
  destinationPorts: DestinationPort[] = ['ALGER', 'ORAN', 'BEJAIA'];
}
