export interface Usuario {
  usuarioId?: number;
  nombre: string;
  username: string;
  email: string;
  passwordHash?: string;
  rol: 'ADMIN' | 'CAJERO' | 'ASESOR';
  estado: 'ACTIVO' | 'INACTIVO';
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  username: string;
  rol: string;
  email: string;
  nombre: string;
}

