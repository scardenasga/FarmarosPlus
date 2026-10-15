import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BotonRetrocederComponent } from './boton-retroceder.component';

describe('BotonRetrocederComponent', () => {
  let component: BotonRetrocederComponent;
  let fixture: ComponentFixture<BotonRetrocederComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BotonRetrocederComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BotonRetrocederComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
