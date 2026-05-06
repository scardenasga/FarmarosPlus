import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BottomNavBarComponent } from './shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { NavigationService } from './shared/services/navigation.service';
import { ThemeService } from './services/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BottomNavBarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
  navService = inject(NavigationService);
  themeService = inject(ThemeService); // Asegura que el servicio se instancie al arrancar
}
