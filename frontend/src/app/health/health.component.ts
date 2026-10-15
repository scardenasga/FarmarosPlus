import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TopBarComponent } from '../shared/components/top-bar/top-bar.component';
import { SearchBarComponent } from '../shared/components/search-bar/search-bar.component';

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [
    TopBarComponent,
    SearchBarComponent
  ],
  templateUrl: './health.component.html',
  styleUrl: './health.component.css'
})
export class HealthComponent {

  private router = inject(Router);

  handleBack(): void {
    this.router.navigate(['/dashboard']);
  }

  handleAlerts(): void {
    this.router.navigate(['/alertas']);
  }
}