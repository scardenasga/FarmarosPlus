import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CierreCajaService, CierreResumen, CierreCaja } from '../../services/cierre-caja.service';
import { AperturaCajaService } from '../../services/apertura-caja.service';
import { NotificacionService } from '../../../shared/services/notificacion.service';

@Component({
  selector: 'app-cierre-caja',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cierre-caja.component.html',
  styles: [`
    .cierre-page {
      display: flex; flex-direction: column; min-height: 100vh;
      background-color: var(--bg-main, var(--background));
      gap: var(--space-m);
    }
    /* Header igual a Gestión de Compras / Ventas */
    .page-header-pc {
      display: flex; align-items: center; justify-content: space-between;
      gap: var(--space-l); padding: var(--space-l) var(--space-xl) var(--space-s); flex-wrap: wrap;
    }
    .header-text h1 { margin:0; font-size:1.3rem; font-weight:800; color: var(--text-dark, var(--on-background)); }
    .header-text p { margin:4px 0 0; font-size:0.72rem; color: var(--text-muted, var(--outline)); font-weight:500; }
    .header-actions { display:flex; align-items:center; gap: var(--space-s); flex-wrap: wrap; }

    .card {
      background: var(--card-bg, var(--surface-container-low));
      border: 1px solid var(--border-color, var(--outline-variant));
      border-radius: 12px; box-shadow: var(--shadow-card, 0 2px 6px rgba(0,0,0,0.04));
      overflow: hidden;
    }
    .card-header {
      display:flex; align-items:center; gap: var(--space-s);
      padding: var(--space-m) var(--space-l); border-bottom:1px solid var(--border-color);
    }
    .card-header h2 { margin:0; font-size:0.84rem; font-weight:800; color: var(--text-dark); }
    .card-header small { margin-left:auto; font-size:0.64rem; color:var(--text-muted); font-weight:700; letter-spacing:0.3px; }
    .card-body { padding: var(--space-m) var(--space-l); }

    /* Contenido con padding lateral igual al dashboard */
    .content-wrap { padding: 0 var(--space-xl) var(--space-l); display:flex; flex-direction:column; gap: var(--space-m); }

    /* KPI strip compacta igual a purchasing-dashboard */
    .kpi-strip { display:flex; gap: var(--space-m); flex-wrap:wrap; }
    .kpi-item {
      display:inline-flex; align-items:center; gap: var(--space-m);
      background: var(--card-bg, #ffffff); border:1px solid var(--border-color, var(--outline-variant));
      border-radius:12px; box-shadow: var(--shadow-card, 0 2px 6px rgba(0,0,0,0.04));
      padding:8px 12px; min-height:48px; flex:1; min-width:160px; max-width:260px;
    }
    .kpi-icon { width:28px; height:28px; border-radius:50%; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
    .kpi-icon svg { width:14px; height:14px; }
    .icon-teal { background: var(--info-bg); color: var(--info-text); }
    .icon-blue { background: var(--purple-bg); color: var(--purple-text); }
    .icon-green { background: var(--success-bg); color: var(--success-text); }
    .icon-gold { background: var(--gold-bg); color: var(--gold-text); }
    .icon-orange { background: var(--orange-bg); color: var(--orange-text); }
    .kpi-texto { display:flex; flex-direction:column; line-height:1.2; min-width:0; }
    .kpi-etiqueta { font-size:0.60rem; text-transform:uppercase; letter-spacing:0.7px; font-weight:800; color: var(--text-muted); white-space:nowrap; }
    .kpi-valor { font-size:1.0rem; font-weight:800; color: var(--text-dark); white-space:nowrap; }
    .kpi-sub { font-size:0.62rem; color: var(--text-muted); font-weight:600; white-space:nowrap; }

    .form-grid { display:grid; grid-template-columns: repeat(auto-fit,minmax(150px,1fr)); gap: var(--space-s); }
    .form-field { display:flex; flex-direction:column; gap:4px; }
    .form-field label { font-size:0.62rem; font-weight:800; text-transform:uppercase; letter-spacing:0.5px; color: var(--text-muted); }
    .form-field input, .form-field textarea {
      height:34px; padding:0 10px; border:1px solid var(--border-color); border-radius:8px;
      background: var(--card-bg); color: var(--text-dark); font-size:0.78rem; font-weight:600;
    }
    .form-field textarea { height:60px; padding:8px 10px; resize: vertical; }
    .form-actions { display:flex; gap: var(--space-s); margin-top: var(--space-s); flex-wrap:wrap; align-items:center; }
    .form-actions input[type="text"] { flex:1; min-width:180px; height:34px; padding:0 10px; border:1px solid var(--border-color); border-radius:8px; background: var(--card-bg); color: var(--text-dark); font-size:0.78rem; font-weight:600; }

    .btn-primary {
      display:inline-flex; align-items:center; gap: var(--space-s);
      padding:8px 14px; border-radius:10px; font-weight:700; font-size:0.74rem;
      background: var(--accent-green-dark, var(--primary-container));
      color: var(--on-primary-container); transition: filter 0.2s;
    }
    .btn-primary:hover { filter: brightness(1.08); }
    .btn-secondary {
      display:inline-flex; align-items:center; gap: var(--space-s);
      padding:8px 12px; border-radius:10px; font-weight:700; font-size:0.74rem;
      border:1px solid var(--border-color, var(--outline-variant));
      background: var(--card-bg, var(--surface-container-low));
      color: var(--text-dark, var(--on-background)); transition: filter 0.2s;
    }
    .btn-secondary:hover { filter: brightness(1.08); }
    .btn-secondary svg, .btn-primary svg { width:14px; height:14px; }

    .badge { display:inline-block; padding:3px 8px; border-radius:9999px; font-size:0.58rem; font-weight:800; text-transform:uppercase; letter-spacing:0.4px; }
    .badge-ok { background: var(--success-bg); color: var(--success-text); }
    .badge-warn { background: var(--warning-bg); color: var(--warning-text); }
    .badge-danger { background: var(--danger-bg); color: var(--danger-text); }

    .inline-diff { display:flex; align-items:center; gap:8px; flex-wrap:wrap; margin-top: var(--space-s); }
    .inline-diff span { font-size:0.68rem; color:var(--text-muted); font-weight:600; }

    .tabla-wrap { border:1px solid var(--border-color); border-radius:10px; overflow:hidden; background: var(--bg-main); max-height:260px; overflow-y:auto; }
    .tabla { width:100%; border-collapse:collapse; min-width:560px; }
    .tabla th { padding:6px var(--space-s); font-size:0.58rem; text-transform:uppercase; letter-spacing:0.6px; font-weight:800; color: var(--text-muted); border-bottom:1px solid var(--border-color); background: var(--card-bg); text-align:left; white-space:nowrap; }
    .tabla td { padding:6px var(--space-s); border-bottom:1px solid var(--border-color); font-size:0.74rem; color: var(--text-dark); }
    .tabla tr:last-child td { border-bottom:none; }
    .empty-state { padding: var(--space-l); text-align:center; color: var(--text-muted); font-weight:600; font-size:0.82rem; }
  `]
})
export class CierreCajaComponent implements OnInit {
  private service = inject(CierreCajaService);
  private apertura = inject(AperturaCajaService);
  private notificacion = inject(NotificacionService);

  desde = signal<string>('');
  hasta = signal<string>('');
  montoInicial = signal<number>(0);
  montoDeclarado = signal<number | null>(null);
  observaciones = signal<string>('');
  idUsuario = signal<number | null>(null);

  resumen = signal<CierreResumen | null>(null);
  cierres = signal<CierreCaja[]>([]);
  cargandoResumen = signal(false);
  guardando = signal(false);

  // Apertura del día (localStorage, una caja / un turno)
  mostrarAperturaDialog = signal(false);
  aperturaInput = signal<number | null>(null);
  aperturaGuardada = signal<boolean>(false);

  diferencia = computed(() => {
    const r = this.resumen();
    const d = this.montoDeclarado();
    if (!r || d == null) return null;
    return d - r.efectivoEsperado;
  });

  ngOnInit(): void {
    const hoy = new Date().toISOString().slice(0, 10);
    this.desde.set(hoy + 'T00:00:00');
    this.hasta.set(hoy + 'T23:59:59');
    // Cargar monto inicial desde apertura guardada del día
    const montoHoy = this.apertura.getMontoHoy();
    if (montoHoy != null) {
      this.montoInicial.set(montoHoy);
      this.aperturaGuardada.set(true);
    } else {
      this.mostrarAperturaDialog.set(true);
      this.aperturaInput.set(montoHoy ?? 0);
    }
    this.consultar();
    this.cargarHistorial();
  }

  guardarApertura(): void {
    const v = this.aperturaInput();
    if (v == null || v < 0) { this.notificacion.error('El monto inicial no puede ser negativo'); return; }
    this.apertura.guardarMontoHoy(Number(v));
    this.montoInicial.set(Number(v));
    this.aperturaGuardada.set(true);
    this.mostrarAperturaDialog.set(false);
    this.notificacion.exito(`Apertura del día guardada: ${Number(v).toLocaleString('es-CO')}`);
    this.consultar();
  }

  cambiarApertura(): void {
    this.aperturaInput.set(this.montoInicial());
    this.mostrarAperturaDialog.set(true);
  }

  onMontoInicialChange(val: number): void {
    this.montoInicial.set(val);
    // Sincroniza apertura del día si ya existe
    if (this.apertura.existeAperturaHoy()) {
      this.apertura.guardarMontoHoy(val);
    }
  }

  consultar(): void {
    this.cargandoResumen.set(true);
    this.service.resumen({
      desde: this.desde() || undefined,
      hasta: this.hasta() || undefined,
      montoInicial: this.montoInicial() || 0
    }).subscribe({
      next: (data) => { this.resumen.set(data); this.cargandoResumen.set(false); },
      error: (err) => { this.notificacion.error(err?.error?.message || 'No se pudo consultar el resumen'); this.cargandoResumen.set(false); }
    });
  }

  cargarHistorial(): void {
    this.service.listar().subscribe({
      next: (data) => this.cierres.set(data || []),
      error: () => {}
    });
  }

  guardarCierre(): void {
    const r = this.resumen();
    if (!r) { this.notificacion.error('Primero consulta el resumen'); return; }
    this.guardando.set(true);
    this.service.crear({
      fechaApertura: r.fechaApertura,
      fechaCierre: r.fechaCierre,
      montoInicial: this.montoInicial() || 0,
      montoDeclarado: this.montoDeclarado(),
      observaciones: this.observaciones() || null
    }).subscribe({
      next: (cierre) => {
        this.guardando.set(false);
        this.notificacion.exito(`Cierre #${cierre.id} guardado · Diferencia ${(cierre.diferencia ?? 0).toFixed(2)}`);
        this.cargarHistorial();
      },
      error: (err) => { this.guardando.set(false); this.notificacion.error(err?.error?.message || 'No se pudo guardar el cierre'); }
    });
  }

  hoy(): void {
    const d = new Date().toISOString().slice(0, 10);
    this.desde.set(d + 'T00:00:00');
    this.hasta.set(d + 'T23:59:59');
    this.consultar();
  }
}
