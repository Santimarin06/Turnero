import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Turno } from '../../models/turno.model';
import { TurnoService } from '../../services/turno.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './turnos.component.html',
  styleUrl: './turnos.component.css'
})
export class TurnosComponent implements OnInit {
  turnos = signal<Turno[]>([]);
  turnoActual = signal<Turno | null>(null);
  siguienteTurno = signal<Turno | null>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  
  isAdmin = signal(false);
  isCajero = signal(false);
  isAsesor = signal(false);

  constructor(
    private turnoService: TurnoService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isAdmin.set(this.authService.isAdmin());
    this.isCajero.set(this.authService.isCajero());
    this.isAsesor.set(this.authService.isAsesor());

    if (this.isCajero()) {
      this.loadTurnosCaja();
      this.loadTurnoActualCaja();
    } else if (this.isAsesor()) {
      this.loadTurnosAsesoria();
      this.loadTurnoActualAsesoria();
    } else if (this.isAdmin()) {
      this.loadTodosLosTurnos();
    }
  }

  loadTurnosCaja(): void {
    this.isLoading.set(true);
    this.turnoService.obtenerPendientesCaja().subscribe({
      next: (turnos) => {
        this.turnos.set(turnos);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar turnos');
        this.isLoading.set(false);
      }
    });
  }

  loadTurnosAsesoria(): void {
    this.isLoading.set(true);
    this.turnoService.obtenerPendientesAsesoria().subscribe({
      next: (turnos) => {
        this.turnos.set(turnos);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar turnos');
        this.isLoading.set(false);
      }
    });
  }

  loadTodosLosTurnos(): void {
    this.isLoading.set(true);
    this.turnoService.obtenerTodos().subscribe({
      next: (turnos) => {
        this.turnos.set(turnos);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar turnos');
        this.isLoading.set(false);
      }
    });
  }

  loadTurnoActualCaja(): void {
    this.turnoService.obtenerTurnoActualCaja().subscribe({
      next: (turno) => this.turnoActual.set(turno),
      error: () => this.turnoActual.set(null)
    });
  }

  loadTurnoActualAsesoria(): void {
    this.turnoService.obtenerTurnoActualAsesoria().subscribe({
      next: (turno) => this.turnoActual.set(turno),
      error: () => this.turnoActual.set(null)
    });
  }

  obtenerSiguienteTurno(): void {
    this.isLoading.set(true);
    if (this.isCajero()) {
      this.turnoService.obtenerSiguienteTurnoCaja().subscribe({
        next: (turno) => {
          this.siguienteTurno.set(turno);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('No hay turnos pendientes');
          this.siguienteTurno.set(null);
          this.isLoading.set(false);
        }
      });
    } else if (this.isAsesor()) {
      this.turnoService.obtenerSiguienteTurnoAsesoria().subscribe({
        next: (turno) => {
          this.siguienteTurno.set(turno);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('No hay turnos pendientes');
          this.siguienteTurno.set(null);
          this.isLoading.set(false);
        }
      });
    }
  }

  iniciarAtencion(turnoId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.turnoService.iniciarAtencion(turnoId).subscribe({
      next: (response) => {
        this.successMessage.set(response || 'Atención iniciada');
        if (this.isCajero()) {
          this.loadTurnosCaja();
          this.loadTurnoActualCaja();
        } else if (this.isAsesor()) {
          this.loadTurnosAsesoria();
          this.loadTurnoActualAsesoria();
        }
        this.siguienteTurno.set(null);
        this.isLoading.set(false);
      },
      error: (error) => {
        const errorMsg = error.error || 'Error al iniciar atención';
        this.errorMessage.set(errorMsg);
        this.isLoading.set(false);
        if (this.isCajero()) {
          this.loadTurnosCaja();
        } else if (this.isAsesor()) {
          this.loadTurnosAsesoria();
        }
      }
    });
  }

  finalizarAtencion(turnoId: number): void {
    this.isLoading.set(true);
    this.turnoService.finalizarAtencion(turnoId).subscribe({
      next: () => {
        this.successMessage.set('Atención finalizada');
        if (this.isCajero()) {
          this.loadTurnosCaja();
          this.loadTurnoActualCaja();
        } else if (this.isAsesor()) {
          this.loadTurnosAsesoria();
          this.loadTurnoActualAsesoria();
        } else if (this.isAdmin()) {
          this.loadTodosLosTurnos();
        }
        this.turnoActual.set(null);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al finalizar atención');
        this.isLoading.set(false);
      }
    });
  }

  cancelarTurno(turnoId: number): void {
    if (confirm('¿Está seguro de cancelar este turno?')) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      this.successMessage.set(null);
      this.turnoService.cancelarTurno(turnoId).subscribe({
        next: (response) => {
          this.successMessage.set(response || 'Turno cancelado correctamente');
          if (this.isCajero()) {
            this.loadTurnosCaja();
            this.loadTurnoActualCaja();
          } else if (this.isAsesor()) {
            this.loadTurnosAsesoria();
            this.loadTurnoActualAsesoria();
          } else if (this.isAdmin()) {
            this.loadTodosLosTurnos();
          }
          this.isLoading.set(false);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al cancelar turno. Intente nuevamente.';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
          if (this.isCajero()) {
            this.loadTurnosCaja();
          } else if (this.isAsesor()) {
            this.loadTurnosAsesoria();
          } else if (this.isAdmin()) {
            this.loadTodosLosTurnos();
          }
        }
      });
    }
  }

  pasarTurnoPrimero(turnoId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.turnoService.pasarTurnoPrimero(turnoId).subscribe({
      next: (response) => {
        this.successMessage.set(response || 'Turno pasado al primero de la cola correctamente');
        this.loadTodosLosTurnos();
        this.isLoading.set(false);
      },
      error: (error) => {
        const errorMsg = error.error || 'Error al pasar turno. Intente nuevamente.';
        this.errorMessage.set(errorMsg);
        this.isLoading.set(false);
        this.loadTodosLosTurnos();
      }
    });
  }

  eliminarTurno(turnoId: number): void {
    if (confirm('¿Está seguro de eliminar este turno permanentemente? Esta acción no se puede deshacer.')) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      this.successMessage.set(null);
      this.turnoService.eliminarTurno(turnoId).subscribe({
        next: (response) => {
          this.successMessage.set(response || 'Turno eliminado correctamente');
          this.loadTodosLosTurnos();
          this.isLoading.set(false);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al eliminar turno. Intente nuevamente.';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
          this.loadTodosLosTurnos();
        }
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
