import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-bottom-nav-bar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: "./bottom-nav-bar.component.html",
  styleUrl:"./bottom-nav-bar.component.css"
})
export class BottomNavBarComponent {

}
