import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputFechaComponent } from './filtro-fecha.component';
import { FormsModule } from '@angular/forms';

describe('InputFechaComponent', () => {
  let component: InputFechaComponent;
  let fixture: ComponentFixture<InputFechaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InputFechaComponent, FormsModule] // Importamos FormsModule aquí también
    })
    .compileComponents();

    fixture = TestBed.createComponent(InputFechaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crear el componente', () => {
    expect(component).toBeTruthy();
  });

  it('debe emitir la fecha cuando cambie', () => {
    spyOn(component.fechaChange, 'emit');
    const eventoMock = { target: { value: '2026-05-20' } };
    component.onDateChange(eventoMock);
    expect(component.fechaChange.emit).toHaveBeenCalledWith('2026-05-20');
  });
});