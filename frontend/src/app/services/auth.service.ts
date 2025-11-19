import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, AuthResponse } from '../models/usuario.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8082/api/auth';
  private currentUserSignal = signal<{ username: string; rol: string; email: string; nombre: string } | null>(null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.checkSession().subscribe();
  }

  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest, { withCredentials: true }).pipe(
      tap(response => {
        this.currentUserSignal.set({
          username: response.username,
          rol: response.rol,
          email: response.email,
          nombre: response.nombre
        });
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap({
        next: () => {
          this.currentUserSignal.set(null);
          this.router.navigate(['/']).then(() => {
            window.location.reload();
          });
        },
        error: () => {
          this.currentUserSignal.set(null);
          this.router.navigate(['/']).then(() => {
            window.location.reload();
          });
        }
      })
    );
  }

  getCurrentUser(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap(response => {
        if (response.usuario) {
          this.currentUserSignal.set({
            username: response.usuario.username,
            rol: response.usuario.rol,
            email: response.usuario.email,
            nombre: response.usuario.nombre
          });
        }
      })
    );
  }

  private sessionChecked = signal(false);

  checkSession(): Observable<any> {
    return this.getCurrentUser().pipe(
      tap({
        next: () => {
          this.sessionChecked.set(true);
        },
        error: () => {
          this.currentUserSignal.set(null);
          this.sessionChecked.set(true);
        }
      })
    );
  }

  isAuthenticated(): boolean {
    return this.currentUserSignal() !== null;
  }

  isSessionChecked(): boolean {
    return this.sessionChecked();
  }

  getCurrentUserSignal() {
    return this.currentUserSignal.asReadonly();
  }

  isAdmin(): boolean {
    const user = this.currentUserSignal();
    return user?.rol === 'ADMIN';
  }

  isCajero(): boolean {
    const user = this.currentUserSignal();
    return user?.rol === 'CAJERO';
  }

  isAsesor(): boolean {
    const user = this.currentUserSignal();
    return user?.rol === 'ASESOR';
  }
}
