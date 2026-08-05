import { TestBed } from '@angular/core/testing';
import { InventoryService } from './inventory.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('InventoryService', () => {
  let service: InventoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(InventoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
