import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario } from '../models/usuario.model';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = 'http://localhost:8082/api/usuario';

  constructor(private http: HttpClient) {}

  obtenerTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.apiUrl}/obtener-todos`, { withCredentials: true });
  }

  obtenerPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  crear(usuario: Usuario): Observable<string> {
    return this.http.post<string>(this.apiUrl, usuario, { 
      responseType: 'text' as 'json',
      withCredentials: true 
    });
  }

  actualizar(usuario: Usuario): Observable<string> {
    return this.http.put<string>(this.apiUrl, usuario, { 
      responseType: 'text' as 'json',
      withCredentials: true 
    });
  }

  eliminar(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`, { 
      responseType: 'text' as 'json',
      withCredentials: true 
    });
  }
}

