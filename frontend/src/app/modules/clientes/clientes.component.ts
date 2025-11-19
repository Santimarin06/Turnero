import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  clientes = signal<Cliente[]>([]);
  clienteForm: FormGroup;
  isEditing = signal(false);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  showForm = signal(false);
  searchDocumento = signal('');
  categorias = ['PREFERENCIAL', 'ADULTO_MAYOR', 'VIP', 'GENERAL'];

  constructor(
    private clienteService: ClienteService,
    private fb: FormBuilder
  ) {
    this.clienteForm = this.fb.group({
      clienteId: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      documento: ['', [Validators.required]],
      telefono: ['', [Validators.required]],
      categoria: ['GENERAL', [Validators.required]],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadClientes();
  }

  loadClientes(): void {
    this.isLoading.set(true);
    this.clienteService.obtenerTodos().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar clientes');
        this.isLoading.set(false);
      }
    });
  }

  openForm(cliente?: Cliente): void {
    if (cliente) {
      this.isEditing.set(true);
      this.clienteForm.patchValue(cliente);
    } else {
      this.isEditing.set(false);
      this.clienteForm.reset({ categoria: 'GENERAL', activo: true });
    }
    this.showForm.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.clienteForm.reset();
    this.isEditing.set(false);
  }

  onSubmit(): void {
    if (this.clienteForm.valid) {
      this.isLoading.set(true);
      const cliente = this.clienteForm.value;

      if (this.isEditing()) {
        this.clienteService.actualizarCliente(cliente).subscribe({
          next: (response) => {
            this.successMessage.set(response || 'Cliente actualizado correctamente');
            this.loadClientes();
            this.closeForm();
            this.isLoading.set(false);
          },
          error: (error) => {
            const errorMsg = error.error || 'Error al actualizar cliente';
            this.errorMessage.set(errorMsg);
            this.isLoading.set(false);
          }
        });
      } else {
        this.clienteService.crearCliente(cliente).subscribe({
          next: (response) => {
            this.successMessage.set(response || 'Cliente creado correctamente');
            this.loadClientes();
            this.closeForm();
            this.isLoading.set(false);
          },
          error: (error) => {
            const errorMsg = error.error || 'Error al crear cliente';
            this.errorMessage.set(errorMsg);
            this.isLoading.set(false);
          }
        });
      }
    }
  }

  buscarPorDocumento(): void {
    if (this.searchDocumento().trim()) {
      this.isLoading.set(true);
      this.clienteService.obtenerPorDocumento(this.searchDocumento()).subscribe({
        next: (cliente) => {
          this.clientes.set([cliente]);
          this.isLoading.set(false);
        },
        error: () => {
          this.errorMessage.set('Cliente no encontrado');
          this.isLoading.set(false);
        }
      });
    } else {
      this.loadClientes();
    }
  }

  inactivarCliente(id: number): void {
    if (confirm('¿Está seguro de inactivar este cliente?')) {
      this.isLoading.set(true);
      this.clienteService.inactivarCliente(id).subscribe({
        next: (response) => {
          this.successMessage.set(response || 'Cliente inactivado correctamente');
          this.loadClientes();
          this.isLoading.set(false);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al inactivar cliente';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
        }
      });
    }
  }

  activarCliente(id: number): void {
    if (confirm('¿Está seguro de activar este cliente?')) {
      this.isLoading.set(true);
      this.clienteService.activarCliente(id).subscribe({
        next: (response) => {
          this.successMessage.set(response || 'Cliente activado correctamente');
          this.loadClientes();
          this.isLoading.set(false);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al activar cliente';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
        }
      });
    }
  }
}

