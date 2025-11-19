import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Turno } from '../models/turno.model';

@Injectable({
  providedIn: 'root',
})
export class TurnoService {
  private apiUrl = 'http://localhost:8082/api/turno';

  constructor(private http: HttpClient) {}

  crearTurno(clienteId: number, motivo: string): Observable<Turno> {
    return this.http.post<Turno>(`${this.apiUrl}/crear/${clienteId}`, { motivo }, { withCredentials: true });
  }

  obtenerSiguienteTurnoCaja(): Observable<Turno> {
    return this.http.get<Turno>(`${this.apiUrl}/caja/siguiente`, { withCredentials: true });
  }

  obtenerTurnoActualCaja(): Observable<Turno> {
    return this.http.get<Turno>(`${this.apiUrl}/caja/actual`, { withCredentials: true });
  }

  obtenerPendientesCaja(): Observable<Turno[]> {
    return this.http.get<Turno[]>(`${this.apiUrl}/caja/pendientes`, { withCredentials: true });
  }

  obtenerSiguienteTurnoAsesoria(): Observable<Turno> {
    return this.http.get<Turno>(`${this.apiUrl}/asesoria/siguiente`, { withCredentials: true });
  }

  obtenerTurnoActualAsesoria(): Observable<Turno> {
    return this.http.get<Turno>(`${this.apiUrl}/asesoria/actual`, { withCredentials: true });
  }

  obtenerPendientesAsesoria(): Observable<Turno[]> {
    return this.http.get<Turno[]>(`${this.apiUrl}/asesoria/pendientes`, { withCredentials: true });
  }

  iniciarAtencion(turnoId: number): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/iniciar-atencion/${turnoId}`, {}, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  finalizarAtencion(turnoId: number): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/finalizar-atencion/${turnoId}`, {}, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  cancelarTurno(turnoId: number): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/cancelar/${turnoId}`, {}, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  obtenerPorId(turnoId: number): Observable<Turno> {
    return this.http.get<Turno>(`${this.apiUrl}/${turnoId}`, { withCredentials: true });
  }

  obtenerTodos(): Observable<Turno[]> {
    return this.http.get<Turno[]>(`${this.apiUrl}/obtener-todos`, { withCredentials: true });
  }

  pasarTurnoPrimero(turnoId: number): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/pasar-primero/${turnoId}`, {}, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  eliminarTurno(turnoId: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${turnoId}`, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }
}

