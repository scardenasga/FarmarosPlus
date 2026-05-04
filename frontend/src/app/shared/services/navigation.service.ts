import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  showBottomNav = signal<boolean>(true);

  hideNav(): void {
    this.showBottomNav.set(false);
  }

  showNav(): void {
    this.showBottomNav.set(true);
  }
}
