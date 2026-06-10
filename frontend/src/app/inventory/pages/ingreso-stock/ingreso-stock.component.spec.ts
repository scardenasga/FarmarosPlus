import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IngresoStockComponent } from './ingreso-stock.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('IngresoStockComponent', () => {
  let component: IngresoStockComponent;
  let fixture: ComponentFixture<IngresoStockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IngresoStockComponent, HttpClientTestingModule, RouterTestingModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(IngresoStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
