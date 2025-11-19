import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TurnoService } from '../../services/turno.service';
import { ClienteService } from '../../services/cliente.service';
import { UsuarioService } from '../../services/usuario.service';
import { Turno } from '../../models/turno.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  totalTurnos = signal(0);
  totalClientes = signal(0);
  totalUsuarios = signal(0);
  turnosPendientes = signal(0);
  turnosEnAtencion = signal(0);
  turnosAtendidos = signal(0);
  isLoading = signal(true);
  isAdmin = signal(false);
  isCajero = signal(false);
  isAsesor = signal(false);
  currentUsername = signal<string>('');

  recentTurnos = signal<Turno[]>([]);
  turnosPendientesList = signal<Turno[]>([]);

  constructor(
    private turnoService: TurnoService,
    private clienteService: ClienteService,
    private usuarioService: UsuarioService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin.set(this.authService.isAdmin());
    this.isCajero.set(this.authService.isCajero());
    this.isAsesor.set(this.authService.isAsesor());
    const currentUser = this.authService.getCurrentUserSignal()();
    this.currentUsername.set(currentUser?.nombre || currentUser?.username || '');
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.isLoading.set(true);

    if (this.isAdmin()) {
      this.turnoService.obtenerTodos().subscribe({
        next: (turnos) => {
          this.totalTurnos.set(turnos.length);
          this.turnosPendientes.set(turnos.filter(t => t.estado === 'PENDIENTE').length);
          this.turnosEnAtencion.set(turnos.filter(t => t.estado === 'EN_ATENCION').length);
          this.turnosAtendidos.set(turnos.filter(t => t.estado === 'ATENDIDO').length);
          this.recentTurnos.set(turnos.slice(-10).reverse());
          this.turnosPendientesList.set(turnos.filter(t => t.estado === 'PENDIENTE'));
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false)
      });

      this.clienteService.obtenerTodos().subscribe({
        next: (clientes) => this.totalClientes.set(clientes.length),
        error: () => {}
      });

      this.usuarioService.obtenerTodos().subscribe({
        next: (usuarios) => this.totalUsuarios.set(usuarios.length),
        error: () => {}
      });
    } else if (this.isCajero()) {
      this.turnoService.obtenerPendientesCaja().subscribe({
        next: (turnos) => {
          this.turnosPendientes.set(turnos.length);
          this.turnosPendientesList.set(turnos);
          this.recentTurnos.set(turnos.slice(-10).reverse());
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false)
      });

      this.turnoService.obtenerTurnoActualCaja().subscribe({
        next: (turno) => { if (turno) this.turnosEnAtencion.set(1); },
        error: () => {}
      });
    } else if (this.isAsesor()) {
      this.turnoService.obtenerPendientesAsesoria().subscribe({
        next: (turnos) => {
          this.turnosPendientes.set(turnos.length);
          this.turnosPendientesList.set(turnos);
          this.recentTurnos.set(turnos.slice(-10).reverse());
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false)
      });

      this.turnoService.obtenerTurnoActualAsesoria().subscribe({
        next: (turno) => { if (turno) this.turnosEnAtencion.set(1); },
        error: () => {}
      });
    }
  }

  formatDate(dateString: string | null | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

