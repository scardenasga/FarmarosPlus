import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {
  showSidebar = signal<boolean>(true);
  isSidebarExpanded = signal<boolean>(false);

  // Alias for backward compatibility
  get showBottomNav() {
    return this.showSidebar;
  }

  toggleSidebar(): void {
    this.isSidebarExpanded.update(val => !val);
  }

  expandSidebar(): void {
    this.isSidebarExpanded.set(true);
  }

  collapseSidebar(): void {
    this.isSidebarExpanded.set(false);
  }

  hideNav(): void {
    this.showSidebar.set(false);
  }

  showNav(): void {
    this.showSidebar.set(true);
  }
}
