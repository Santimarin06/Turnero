import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Cliente } from '../models/cliente.model';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private apiUrl = 'http://localhost:8082/api/cliente';

  constructor(private http: HttpClient) {}

  crearCliente(cliente: Cliente): Observable<string> {
    return this.http.post<string>(this.apiUrl, cliente, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  obtenerTodos(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.apiUrl}/obtener-todos`, { withCredentials: true });
  }

  obtenerPorId(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  obtenerPorDocumento(documento: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.apiUrl}/documento/${documento}`, { 
      withCredentials: true 
    });
  }

  actualizarCliente(cliente: Cliente): Observable<string> {
    return this.http.put<string>(this.apiUrl, cliente, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  inactivarCliente(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`, { 
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  activarCliente(id: number): Observable<string> {
    return this.http.get<Cliente>(`${this.apiUrl}/${id}`, { withCredentials: true }).pipe(
      switchMap(cliente => {
        cliente.activo = true;
        return this.actualizarCliente(cliente);
      })
    );
  }
}

