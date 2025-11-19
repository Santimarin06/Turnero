import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Usuario } from '../../models/usuario.model';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  usuarios = signal<Usuario[]>([]);
  usuarioForm: FormGroup;
  isEditing = signal(false);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  showForm = signal(false);

  roles = ['ADMIN', 'CAJERO', 'ASESOR'];
  estados = ['ACTIVO', 'INACTIVO'];

  constructor(
    private usuarioService: UsuarioService,
    private fb: FormBuilder
  ) {
    this.usuarioForm = this.fb.group({
      usuarioId: [null],
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      passwordHash: ['', [Validators.minLength(6)]],
      rol: ['CAJERO', [Validators.required]],
      estado: ['ACTIVO', [Validators.required]]
    });

    this.usuarioForm.get('usuarioId')?.valueChanges.subscribe(id => {
      const passwordControl = this.usuarioForm.get('passwordHash');
      if (id) {
        passwordControl?.clearValidators();
      } else {
        passwordControl?.setValidators([Validators.required, Validators.minLength(6)]);
      }
      passwordControl?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    this.loadUsuarios();
  }

  loadUsuarios(): void {
    this.isLoading.set(true);
    this.usuarioService.obtenerTodos().subscribe({
      next: (usuarios) => {
        this.usuarios.set(usuarios);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Error al cargar usuarios');
        this.isLoading.set(false);
      }
    });
  }

  openForm(usuario?: Usuario): void {
    if (usuario) {
      this.isEditing.set(true);
      this.usuarioForm.patchValue({ ...usuario, passwordHash: '' });
    } else {
      this.isEditing.set(false);
      this.usuarioForm.reset({ rol: 'CAJERO', estado: 'ACTIVO' });
    }
    this.showForm.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.usuarioForm.reset();
    this.isEditing.set(false);
  }

  onSubmit(): void {
    if (this.usuarioForm.valid) {
      this.isLoading.set(true);
      const usuario = this.usuarioForm.value;

      if (this.isEditing()) {
        this.usuarioService.actualizar(usuario).subscribe({
          next: (response) => {
            this.successMessage.set(response || 'Usuario actualizado correctamente');
            this.loadUsuarios();
            this.closeForm();
            this.isLoading.set(false);
          },
          error: (error) => {
            const errorMsg = error.error || 'Error al actualizar usuario';
            this.errorMessage.set(errorMsg);
            this.isLoading.set(false);
          }
        });
      } else {
        this.usuarioService.crear(usuario).subscribe({
          next: (response) => {
            this.successMessage.set(response || 'Usuario creado correctamente');
            this.loadUsuarios();
            this.closeForm();
            this.isLoading.set(false);
          },
          error: (error) => {
            const errorMsg = error.error || 'Error al crear usuario';
            this.errorMessage.set(errorMsg);
            this.isLoading.set(false);
          }
        });
      }
    }
  }

  eliminarUsuario(id: number): void {
    if (confirm('¿Está seguro de eliminar este usuario?')) {
      this.isLoading.set(true);
      this.usuarioService.eliminar(id).subscribe({
        next: (response) => {
          this.successMessage.set(response || 'Usuario eliminado correctamente');
          this.loadUsuarios();
          this.isLoading.set(false);
        },
        error: (error) => {
          const errorMsg = error.error || 'Error al eliminar usuario';
          this.errorMessage.set(errorMsg);
          this.isLoading.set(false);
        }
      });
    }
  }
}

