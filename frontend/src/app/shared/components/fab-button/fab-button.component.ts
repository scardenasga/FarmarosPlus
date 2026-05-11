import { Component, output } from '@angular/core';

@Component({
  selector: 'app-fab-button',
  standalone: true,
  templateUrl: "./fab-button.component.html",
  styleUrl: "./fab-button.component.css"
})
export class FabButtonComponent {
  clicked = output<void>();
}
