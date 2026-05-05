import { Component, input, output, OnInit, OnDestroy } from '@angular/core';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [],
  templateUrl: './search-bar.component.html',
styleUrl: './search-bar.component.css'
})
export class SearchBarComponent implements OnInit, OnDestroy {
  placeholder = input<string>('Buscar...');
  initialValue = input<string>('');
  debounceMs = input<number>(300);

  search = output<string>();

  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(this.debounceMs()),
      distinctUntilChanged()
    ).subscribe(value => {
      this.search.emit(value);
    });
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  onInput(event: Event): void {
    const element = event.target as HTMLInputElement;
    this.searchSubject.next(element.value);
  }
}
