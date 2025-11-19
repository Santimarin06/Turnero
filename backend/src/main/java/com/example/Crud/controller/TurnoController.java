package com.example.Crud.controller;

import com.example.Crud.Entidad.Turno;
import com.example.Crud.Service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/turno")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @PostMapping("/crear/{clienteId}")
    public ResponseEntity<?> crearTurno(@PathVariable("clienteId") Long clienteId, 
                                        @RequestBody Map<String, String> request) {
        try {
            String motivo = request.get("motivo");
            if (motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El motivo es requerido");
            }
            
            Turno turno = turnoService.crearTurno(clienteId, motivo);
            return ResponseEntity.ok(turno);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/caja/siguiente")
    public ResponseEntity<?> obtenerSiguienteTurnoCaja() {
        Optional<Turno> turno = turnoService.obtenerSiguienteTurnoCaja();
        if (turno.isPresent()) {
            return ResponseEntity.ok(turno.get());
        } else {
            List<Turno> pendientes = turnoService.obtenerTurnosPendientesPorTipoCola(Turno.TipoCola.CAJA);
            if (pendientes.isEmpty()) {
                return ResponseEntity.status(204).body("No hay turnos pendientes en la cola de CAJA");
            } else {
                return ResponseEntity.status(204).body("No se pudo obtener el siguiente turno. Hay " + pendientes.size() + " turnos pendientes.");
            }
        }
    }

    @GetMapping("/caja/actual")
    public ResponseEntity<Turno> obtenerTurnoActualCaja() {
        Optional<Turno> turno = turnoService.obtenerTurnoActualCaja();
        return turno.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/asesoria/siguiente")
    public ResponseEntity<Turno> obtenerSiguienteTurnoAsesoria() {
        Optional<Turno> turno = turnoService.obtenerSiguienteTurnoAsesoria();
        return turno.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/asesoria/actual")
    public ResponseEntity<Turno> obtenerTurnoActualAsesoria() {
        Optional<Turno> turno = turnoService.obtenerTurnoActualAsesoria();
        return turno.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/caja/pendientes")
    public List<Turno> obtenerTurnosPendientesCaja() {
        return turnoService.obtenerTurnosPendientesPorTipoCola(Turno.TipoCola.CAJA);
    }

    @GetMapping("/asesoria/pendientes")
    public List<Turno> obtenerTurnosPendientesAsesoria() {
        return turnoService.obtenerTurnosPendientesPorTipoCola(Turno.TipoCola.ASESORIA);
    }

    @GetMapping("/obtener-todos")
    public List<Turno> obtenerTodosLosTurnos() {
        return turnoService.obtenerTodosLosTurnos();
    }

    @GetMapping("/{turnoId}")
    public ResponseEntity<Turno> obtenerTurnoPorId(@PathVariable("turnoId") Long turnoId) {
        Optional<Turno> turno = turnoService.obtenerTurnoPorId(turnoId);
        return turno.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public List<Turno> obtenerTurnosPorEstado(@PathVariable("estado") String estado) {
        try {
            Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado.toUpperCase());
            return turnoService.obtenerTurnosPorEstado(estadoEnum);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @PutMapping("/iniciar-atencion/{turnoId}")
    public ResponseEntity<String> iniciarAtencion(@PathVariable("turnoId") Long turnoId) {
        Optional<Turno> turnoOpt = turnoService.obtenerTurnoPorId(turnoId);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Turno no encontrado");
        }
        
        Turno turno = turnoOpt.get();
        if (turno.getEstado() != Turno.EstadoTurno.PENDIENTE) {
            return ResponseEntity.badRequest().body("Error: El turno no está en estado PENDIENTE. Estado actual: " + turno.getEstado());
        }
        
        if (turnoService.iniciarAtencion(turnoId)) {
            return ResponseEntity.ok("Atención iniciada correctamente");
        }
        return ResponseEntity.badRequest().body("Error: No se pudo iniciar la atención del turno");
    }

    @PutMapping("/finalizar-atencion/{turnoId}")
    public ResponseEntity<String> finalizarAtencion(@PathVariable("turnoId") Long turnoId) {
        if (turnoService.finalizarAtencion(turnoId)) {
            return ResponseEntity.ok("Atención finalizada correctamente");
        }
        return ResponseEntity.badRequest().body("Error: No se pudo finalizar la atención del turno");
    }

    @PutMapping("/cancelar/{turnoId}")
    public ResponseEntity<String> cancelarTurno(@PathVariable("turnoId") Long turnoId) {
        Optional<Turno> turnoOpt = turnoService.obtenerTurnoPorId(turnoId);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Turno no encontrado");
        }
        Turno turno = turnoOpt.get();
        if (turno.getEstado() == Turno.EstadoTurno.CANCELADO) {
            return ResponseEntity.badRequest().body("Error: El turno ya está cancelado");
        }
        if (turnoService.cancelarTurno(turnoId)) {
            return ResponseEntity.ok("Turno cancelado correctamente");
        }
        return ResponseEntity.badRequest().body("Error: No se pudo cancelar el turno");
    }

    @PutMapping("/pasar-primero/{turnoId}")
    public ResponseEntity<String> pasarTurnoPrimero(@PathVariable("turnoId") Long turnoId) {
        if (turnoService.pasarTurnoPrimero(turnoId)) {
            return ResponseEntity.ok("Turno pasado al primero de la cola correctamente");
        }
        return ResponseEntity.badRequest().body("Error: No se pudo pasar el turno al primero");
    }

    @DeleteMapping("/{turnoId}")
    public ResponseEntity<String> eliminarTurno(@PathVariable("turnoId") Long turnoId) {
        Optional<Turno> turnoOpt = turnoService.obtenerTurnoPorId(turnoId);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Turno no encontrado");
        }
        Turno turno = turnoOpt.get();
        if (turno.getEstado() != Turno.EstadoTurno.CANCELADO && 
            turno.getEstado() != Turno.EstadoTurno.ATENDIDO) {
            return ResponseEntity.badRequest().body("Error: Solo se pueden eliminar turnos cancelados o ya atendidos");
        }
        if (turnoService.eliminarTurno(turnoId)) {
            return ResponseEntity.ok("Turno eliminado correctamente");
        }
        return ResponseEntity.badRequest().body("Error: No se pudo eliminar el turno");
    }

    @PostMapping("/actualizar-aging")
    public ResponseEntity<String> actualizarAging() {
        turnoService.actualizarAgingTurnos();
        return ResponseEntity.ok("Prioridades actualizadas correctamente");
    }
}
