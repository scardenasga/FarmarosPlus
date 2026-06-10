import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InventorySortComponent } from './inventory-sort.component';

describe('InventorySortComponent', () => {
  let component: InventorySortComponent;
  let fixture: ComponentFixture<InventorySortComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventorySortComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InventorySortComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
