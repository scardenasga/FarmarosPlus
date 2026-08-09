import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ActiveFilter {
  id: string; // e.g., 'category_1', 'minPrice', etc.
  label: string;
  type: 'category' | 'state' | 'price' | 'margin' | 'expiration' | 'search';
}

@Component({
  selector: 'app-active-filters',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-filters.component.html',
  styleUrl: './active-filters.component.css'
})
export class ActiveFiltersComponent {
  filters = input<ActiveFilter[]>([]);
  remove = output<ActiveFilter>();
}
