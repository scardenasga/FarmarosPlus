import { Component, input, output, inject } from '@angular/core';
import { AlertaService } from '../../../services/alerta.service';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  templateUrl: './top-bar.component.html',
  styleUrl: './top-bar.component.css'
})
export class TopBarComponent {
  title = input<string>('');
  showAlert = input<boolean>(true);
  back = output<void>();
  alert = output<void>();

  alertaService = inject(AlertaService);
}
