import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GestionLotesComponent } from './gestion-lotes.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('GestionLotesComponent', () => {
  let component: GestionLotesComponent;
  let fixture: ComponentFixture<GestionLotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionLotesComponent, HttpClientTestingModule, RouterTestingModule]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(GestionLotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
