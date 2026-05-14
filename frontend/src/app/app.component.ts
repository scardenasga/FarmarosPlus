import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BottomNavBarComponent } from './shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { NavigationService } from './shared/services/navigation.service';
import { ThemeService } from './services/theme.service';
import { AlertaService } from './services/alerta.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BottomNavBarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'frontend';
  navService = inject(NavigationService);
  themeService = inject(ThemeService);
  private alertaService = inject(AlertaService);

  ngOnInit(): void {
    this.alertaService.generarYActualizar();
  }
}
