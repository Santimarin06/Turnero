import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ClienteService } from '../../services/cliente.service';
import { TurnoService } from '../../services/turno.service';
import { Cliente } from '../../models/cliente.model';
import { Turno } from '../../models/turno.model';

@Component({
  selector: 'app-public',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './public.component.html',
  styleUrl: './public.component.css'
})
export class PublicComponent {
  clienteForm: FormGroup;
  turnoForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  turnoGenerado = signal<Turno | null>(null);
  clienteEncontrado = signal<Cliente | null>(null);
  showTurnoForm = signal(false);
  searchDocumento = signal('');

  categorias = ['GENERAL', 'PREFERENCIAL', 'ADULTO_MAYOR', 'VIP'];

  constructor(
    private fb: FormBuilder,
    private clienteService: ClienteService,
    private turnoService: TurnoService,
    private router: Router
  ) {
    this.clienteForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      documento: ['', [Validators.required]],
      telefono: ['', [Validators.required]],
      categoria: ['GENERAL', [Validators.required]]
    });

    this.turnoForm = this.fb.group({
      motivo: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  buscarCliente(): void {
    const documento = this.searchDocumento().trim();
    if (documento) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      this.successMessage.set(null);
      this.clienteService.obtenerPorDocumento(documento).subscribe({
        next: (cliente) => {
          if (!cliente.activo) {
            this.errorMessage.set('No se puede generar un turno porque el cliente está inactivado. Por favor, contacte con un administrador.');
            this.clienteEncontrado.set(null);
            this.showTurnoForm.set(false);
          } else {
            this.clienteEncontrado.set(cliente);
            this.showTurnoForm.set(true);
            this.successMessage.set(null);
          }
          this.isLoading.set(false);
        },
        error: (error) => {
          if (error.status === 404) {
            this.errorMessage.set('Cliente no encontrado. Por favor, regístrese primero.');
          } else {
            this.errorMessage.set('Error al buscar cliente. Intente nuevamente.');
          }
          this.clienteEncontrado.set(null);
          this.showTurnoForm.set(false);
          this.isLoading.set(false);
        }
      });
    } else {
      this.errorMessage.set('Por favor, ingrese un número de documento');
    }
  }

  registrarCliente(): void {
    if (this.clienteForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      const documento = this.clienteForm.get('documento')?.value;
      this.clienteService.crearCliente(this.clienteForm.value).subscribe({
        next: () => {
          this.successMessage.set('Cliente registrado correctamente');
          this.searchDocumento.set(documento);
          setTimeout(() => {
            this.buscarCliente();
          }, 500);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al registrar cliente. Intente nuevamente.';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
        }
      });
    }
  }

  crearTurno(): void {
    if (this.turnoForm.valid && this.clienteEncontrado()) {
      if (!this.clienteEncontrado()!.activo) {
        this.errorMessage.set('No se puede generar un turno porque el cliente está inactivado. Por favor, contacte con un administrador.');
        return;
      }

      this.isLoading.set(true);
      this.errorMessage.set(null);
      this.successMessage.set(null);
      const motivo = this.turnoForm.get('motivo')?.value;
      
      this.turnoService.crearTurno(this.clienteEncontrado()!.clienteId!, motivo).subscribe({
        next: (turno) => {
          this.turnoGenerado.set(turno);
          this.successMessage.set(`¡Turno ${turno.numeroTurno} generado exitosamente!`);
          this.turnoForm.reset();
          this.showTurnoForm.set(false);
          this.isLoading.set(false);
        },
        error: (error) => {
          let errorMsg = 'Error al generar turno. Intente nuevamente.';
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMsg = error.error;
            } else if (error.error.message) {
              errorMsg = error.error.message;
            }
          }
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
        }
      });
    }
  }

  nuevoTurno(): void {
    this.turnoGenerado.set(null);
    this.clienteEncontrado.set(null);
    this.searchDocumento.set('');
    this.showTurnoForm.set(false);
    this.clienteForm.reset({ categoria: 'GENERAL' });
    this.turnoForm.reset();
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  cancelarTurno(): void {
    this.clienteEncontrado.set(null);
    this.showTurnoForm.set(false);
    this.turnoForm.reset();
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  irAlSistema(): void {
    this.router.navigate(['/login']);
  }
}
