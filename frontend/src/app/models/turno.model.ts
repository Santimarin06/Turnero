import { Cliente } from './cliente.model';

export interface Turno {
  turnoId?: number;
  numeroTurno: string;
  cliente: Cliente;
  estado: 'PENDIENTE' | 'EN_ATENCION' | 'ATENDIDO' | 'CANCELADO';
  prioridad: number;
  fechaCreacion?: string;
  fechaAtencion?: string;
  tiempoEsperaMinutos?: number;
  ultimaActualizacionPrioridad?: string;
  contadorAging?: number;
  motivo: string;
  tipoCola: 'CAJA' | 'ASESORIA';
}

