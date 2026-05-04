import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EstadoVentaComponent } from './estado-venta.component'; // <--- Nombre corregido

describe('EstadoVentaComponent', () => {
  let component: EstadoVentaComponent;
  let fixture: ComponentFixture<EstadoVentaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstadoVentaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstadoVentaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});