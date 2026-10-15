import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './form-input.component.html',
  styleUrl: './form-input.component.css'
})
export class FormInputComponent {
  label = input.required<string>();
  placeholder = input<string>('');
  type = input<string>('text'); // text, number, select, textarea
  control = input.required<FormControl>();
  options = input<{id: any, nombre: string}[] | null>(null);
  suffix = input<string>('');
  hasIcon = input<boolean>(false);
}
