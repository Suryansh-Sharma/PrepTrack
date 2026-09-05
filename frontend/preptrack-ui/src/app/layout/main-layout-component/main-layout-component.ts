import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar-component/navbar-component';
import { SidebarComponent } from '../sidebar-component/sidebar-component';

@Component({
  selector: 'pt-main-layout-component',
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './main-layout-component.html',
  styleUrl: './main-layout-component.css',
})
export class MainLayoutComponent {}
