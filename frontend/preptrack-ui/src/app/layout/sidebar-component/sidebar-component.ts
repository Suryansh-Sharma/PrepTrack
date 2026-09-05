import { Component, OnInit, signal } from '@angular/core';
import { AuthService } from '../../features/auth/services/auth.service';
import { MeResponseDto } from '../../features/auth/models/auth.common.models';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'pt-sidebar-component',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar-component.html',
  styleUrl: './sidebar-component.css',
})
export class SidebarComponent implements OnInit {
  userInfo = signal<MeResponseDto | null>(null);

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.getCurrentUserInfo();
  }

  getCurrentUserInfo() {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.userInfo.set(user);
      },
      error: (err) => {
        console.error('Failed to load user info in sidebar', err);
      },
    });
  }

  getInitials(name: string | undefined | null): string {
    if (!name) return '??';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }
}
