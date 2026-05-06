import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NotificacionPedidosComponent } from './notificacion-pedidos.component';
import { CompraService } from '../../../ventas/services/compra.service';

describe('NotificacionPedidosComponent', () => {
  let component: NotificacionPedidosComponent;
  let fixture: ComponentFixture<NotificacionPedidosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NotificacionPedidosComponent,
        HttpClientTestingModule // Importante para que no falle por falta de HTTP
      ],
      providers: [CompraService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificacionPedidosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
