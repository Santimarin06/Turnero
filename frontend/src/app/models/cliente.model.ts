export interface Cliente {
  clienteId?: number;
  nombre: string;
  documento: string;
  telefono: string;
  categoria: 'PREFERENCIAL' | 'ADULTO_MAYOR' | 'VIP' | 'GENERAL';
  fechaRegistro?: string;
  activo?: boolean;
}

