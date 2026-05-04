import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FiltrarHistorialComponent } from './filtrar-historial.component';

describe('FiltrarHistorialComponent', () => {
  let component: FiltrarHistorialComponent;
  let fixture: ComponentFixture<FiltrarHistorialComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FiltrarHistorialComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FiltrarHistorialComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
