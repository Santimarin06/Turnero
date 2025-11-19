import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-system-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './system-layout.component.html',
  styleUrl: './system-layout.component.css'
})
export class SystemLayoutComponent {
  constructor(public authService: AuthService) {}

  currentUser = computed(() => this.authService.getCurrentUserSignal()());

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isCajero(): boolean {
    return this.authService.isCajero();
  }

  isAsesor(): boolean {
    return this.authService.isAsesor();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {},
      error: () => {}
    });
  }
}

