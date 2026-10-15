import { Injectable, signal, effect } from '@angular/core';

export type ThemeType = 'dark-theme' | 'light-theme' | 'ligth-blue-theme' | 'dark-blue-theme' | 'ligth-green-theme' | 'dark-green-theme' | 'sepia-theme' | 'high-contrast-theme';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'app-theme';
  private currentThemeValue: ThemeType;
  theme = signal<ThemeType>(this.getStoredTheme());

  constructor() {
    this.currentThemeValue = this.theme();
    // Aplicar inmediatamente al instanciar el servicio
    this.applyTheme(this.currentThemeValue);
    
    // Sincronizar el signal con el DOM
    effect(() => {
      const newTheme = this.theme();
      this.applyTheme(newTheme);
    });
  }

  setTheme(theme: ThemeType) {
    this.theme.set(theme);
    localStorage.setItem(this.THEME_KEY, theme);
  }

  private getStoredTheme(): ThemeType {
    const stored = localStorage.getItem(this.THEME_KEY) as ThemeType;
    return stored || 'dark-theme';
  }

  private applyTheme(theme: ThemeType) {
    if (typeof document === 'undefined') return; // Seguridad para SSR si se aplica después

    const body = document.body;
    const themeClasses = [
      'dark-theme', 
      'light-theme', 
      'ligth-blue-theme', 
      'dark-blue-theme', 
      'ligth-green-theme', 
      'dark-green-theme',
      'sepia-theme',
      'high-contrast-theme'
    ];
    
    // Limpiar clases previas y añadir la nueva
    body.classList.remove(...themeClasses);
    body.classList.add(theme);
  }
}
