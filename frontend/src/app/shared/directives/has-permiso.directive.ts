import { Directive, TemplateRef, ViewContainerRef, input, effect, inject } from '@angular/core';
import { PermisoService } from '../services/permiso.service';

@Directive({ selector: '[hasPermiso]', standalone: true })
export class HasPermisoDirective {
  private template = inject(TemplateRef<any>);
  private vcr = inject(ViewContainerRef);
  private permisoService = inject(PermisoService);
  private hasView = false;

  permiso = input.required<string | string[]>({ alias: 'hasPermiso' });

  constructor() {
    effect(() => {
      const val = this.permiso();
      const claves = Array.isArray(val) ? val : [val];
      const ok = claves.some(c => this.permisoService.tiene(c));
      if (ok && !this.hasView) {
        this.vcr.createEmbeddedView(this.template);
        this.hasView = true;
      } else if (!ok && this.hasView) {
        this.vcr.clear();
        this.hasView = false;
      }
    });
  }
}
