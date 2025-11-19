package com.example.Crud.Service;

import com.example.Crud.Entidad.Turno;
import com.example.Crud.Entidad.Cliente;
import com.example.Crud.repository.TurnoRepository;
import com.example.Crud.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private static final int PRIORIDAD_PREFERENCIAL = 1000;
    private static final int PRIORIDAD_ADULTO_MAYOR = 800;
    private static final int PRIORIDAD_VIP = 600;
    private static final int PRIORIDAD_GENERAL = 100;
    
    private static final int INCREMENTO_AGING = 50;
    
    private static final int LIMITE_PREFERENCIALES = 3;
    
    private int contadorEspecialesCaja = 0;
    private int contadorEspecialesAsesoria = 0;

    private Turno.TipoCola determinarTipoCola(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            return Turno.TipoCola.CAJA;
        }
        
        String motivoLower = motivo.toLowerCase();
        
        String[] palabrasCaja = {"deposito", "depósito", "retiro", "pago", "transferencia", 
                                  "efectivo", "cobro", "giro", "cheque", "cuenta"};
        
        String[] palabrasAsesoria = {"consulta", "asesoria", "asesoría", "credito", "crédito", 
                                     "prestamo", "préstamo", "inversion", "inversión", 
                                     "producto", "tarjeta", "seguro", "plan"};
        
        for (String palabra : palabrasCaja) {
            if (motivoLower.contains(palabra)) {
                return Turno.TipoCola.CAJA;
            }
        }
        
        for (String palabra : palabrasAsesoria) {
            if (motivoLower.contains(palabra)) {
                return Turno.TipoCola.ASESORIA;
            }
        }
        
        return Turno.TipoCola.CAJA;
    }

    private int calcularPrioridadInicial(Cliente.CategoriaCliente categoria) {
        return switch (categoria) {
            case PREFERENCIAL -> PRIORIDAD_PREFERENCIAL;
            case ADULTO_MAYOR -> PRIORIDAD_ADULTO_MAYOR;
            case VIP -> PRIORIDAD_VIP;
            case GENERAL -> PRIORIDAD_GENERAL;
        };
    }

    private String generarNumeroTurno(Cliente.CategoriaCliente categoria, Long contador, Turno.TipoCola tipoCola) {
        String prefijo = switch (categoria) {
            case PREFERENCIAL -> "P";
            case ADULTO_MAYOR -> "A";
            case VIP -> "V";
            case GENERAL -> "G";
        };
        
        String sufijo = tipoCola == Turno.TipoCola.CAJA ? "C" : "AS";
        
        return String.format("%s%s%03d", prefijo, sufijo, contador);
    }

    @Transactional
    public Turno crearTurno(Long clienteId, String motivo) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        if (!clienteOpt.get().getActivo()) {
            throw new RuntimeException("No se puede generar un turno porque el cliente está inactivado");
        }

        Cliente cliente = clienteOpt.get();
        
        List<Turno> turnosPendientes = turnoRepository.findByClienteIdAndEstado(
            clienteId, 
            Turno.EstadoTurno.PENDIENTE
        );
        List<Turno> turnosEnAtencion = turnoRepository.findByClienteIdAndEstado(
            clienteId, 
            Turno.EstadoTurno.EN_ATENCION
        );
        if (!turnosPendientes.isEmpty()) {
            throw new RuntimeException("El cliente ya tiene un turno pendiente. Debe esperar a que se atienda o cancele su turno actual antes de generar uno nuevo.");
        }
        if (!turnosEnAtencion.isEmpty()) {
            throw new RuntimeException("El cliente ya tiene un turno en atención. Debe esperar a que se finalice o cancele su turno actual antes de generar uno nuevo.");
        }
        Turno.TipoCola tipoCola = determinarTipoCola(motivo);
        
        Long contador = turnoRepository.countByCategoriaAndTipoCola(
            cliente.getCategoria(), 
            tipoCola
        ) + 1;

        String numeroTurno;
        int intentos = 0;
        do {
            numeroTurno = generarNumeroTurno(cliente.getCategoria(), contador, tipoCola);
            Optional<Turno> turnoExistente = turnoRepository.findByNumeroTurno(numeroTurno);
            if (turnoExistente.isEmpty()) {
                break;
            }
            contador++;
            intentos++;
            if (intentos > 100) {
                throw new RuntimeException("Error: No se pudo generar un número de turno único después de múltiples intentos");
            }
        } while (true);

        Turno turno = new Turno();
        turno.setCliente(cliente);
        turno.setNumeroTurno(numeroTurno);
        turno.setEstado(Turno.EstadoTurno.PENDIENTE);
        turno.setPrioridad(calcularPrioridadInicial(cliente.getCategoria()));
        turno.setFechaCreacion(LocalDateTime.now());
        turno.setUltimaActualizacionPrioridad(LocalDateTime.now());
        turno.setContadorAging(0);
        turno.setTiempoEsperaMinutos(0L);
        turno.setMotivo(motivo);
        turno.setTipoCola(tipoCola);

        return turnoRepository.save(turno);
    }

    @Transactional
    public Optional<Turno> obtenerSiguienteTurnoCaja() {
        List<Turno> turnosPendientes = turnoRepository.findPendientesPorTipoCola(
            Turno.EstadoTurno.PENDIENTE,
            Turno.TipoCola.CAJA
        );

        if (turnosPendientes.isEmpty()) {
            return Optional.empty();
        }

        return aplicarLimiteEspeciales(turnosPendientes, Turno.TipoCola.CAJA);
    }

    @Transactional
    public Optional<Turno> obtenerSiguienteTurnoAsesoria() {
        List<Turno> turnosPendientes = turnoRepository.findPendientesPorTipoCola(
            Turno.EstadoTurno.PENDIENTE,
            Turno.TipoCola.ASESORIA
        );

        if (turnosPendientes.isEmpty()) {
            return Optional.empty();
        }

        return aplicarLimiteEspeciales(turnosPendientes, Turno.TipoCola.ASESORIA);
    }

    private boolean esCategoriaEspecial(Cliente.CategoriaCliente categoria) {
        return categoria == Cliente.CategoriaCliente.VIP ||
               categoria == Cliente.CategoriaCliente.PREFERENCIAL ||
               categoria == Cliente.CategoriaCliente.ADULTO_MAYOR;
    }

    private Optional<Turno> aplicarLimiteEspeciales(List<Turno> turnosPendientes, Turno.TipoCola tipoCola) {
        int contadorEspeciales = (tipoCola == Turno.TipoCola.CAJA) ? contadorEspecialesCaja : contadorEspecialesAsesoria;
        
        if (contadorEspeciales >= 3) {
            for (Turno turno : turnosPendientes) {
                if (turno.getCliente().getCategoria() == Cliente.CategoriaCliente.GENERAL) {
                    return Optional.of(turno);
                }
            }
        }
        
        return Optional.of(turnosPendientes.get(0));
    }

    @Transactional
    public void actualizarAgingTurnos() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusMinutes(5);
        
        actualizarAgingPorTipoCola(Turno.TipoCola.CAJA, fechaLimite);
        
        actualizarAgingPorTipoCola(Turno.TipoCola.ASESORIA, fechaLimite);
    }

    private void actualizarAgingPorTipoCola(Turno.TipoCola tipoCola, LocalDateTime fechaLimite) {
        List<Turno> turnosParaAging = turnoRepository.findTurnosParaAgingPorTipoCola(
            Turno.EstadoTurno.PENDIENTE,
            tipoCola,
            Cliente.CategoriaCliente.GENERAL,
            fechaLimite
        );

        for (Turno turno : turnosParaAging) {
            if (turno.getUltimaActualizacionPrioridad() != null) {
                long minutosTranscurridos = ChronoUnit.MINUTES.between(
                    turno.getUltimaActualizacionPrioridad(), 
                    LocalDateTime.now()
                );
                
                int incrementos = (int) (minutosTranscurridos / 5);
                if (incrementos > 0) {
                    turno.setPrioridad(turno.getPrioridad() + (incrementos * INCREMENTO_AGING));
                    turno.setContadorAging(turno.getContadorAging() + incrementos);
                    turno.setUltimaActualizacionPrioridad(LocalDateTime.now());
                    turnoRepository.save(turno);
                }
            }
        }
    }

    @Transactional
    public boolean iniciarAtencion(Long turnoId) {
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent() && turnoOpt.get().getEstado() == Turno.EstadoTurno.PENDIENTE) {
            Turno turno = turnoOpt.get();
            turno.setEstado(Turno.EstadoTurno.EN_ATENCION);
            turno.setFechaAtencion(LocalDateTime.now());
            
            if (turno.getFechaCreacion() != null) {
                long minutosEspera = ChronoUnit.MINUTES.between(
                    turno.getFechaCreacion(), 
                    turno.getFechaAtencion()
                );
                turno.setTiempoEsperaMinutos(minutosEspera);
            }
            
            turnoRepository.save(turno);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean finalizarAtencion(Long turnoId) {
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent() && turnoOpt.get().getEstado() == Turno.EstadoTurno.EN_ATENCION) {
            Turno turno = turnoOpt.get();
            Turno.TipoCola tipoCola = turno.getTipoCola();
            Cliente.CategoriaCliente categoria = turno.getCliente().getCategoria();
            
            turno.setEstado(Turno.EstadoTurno.ATENDIDO);
            turnoRepository.save(turno);
            
            if (categoria == Cliente.CategoriaCliente.GENERAL) {
                if (tipoCola == Turno.TipoCola.CAJA) {
                    contadorEspecialesCaja = 0;
                } else {
                    contadorEspecialesAsesoria = 0;
                }
            } else if (esCategoriaEspecial(categoria)) {
                if (tipoCola == Turno.TipoCola.CAJA) {
                    contadorEspecialesCaja++;
                } else {
                    contadorEspecialesAsesoria++;
                }
            }
            
            return true;
        }
        return false;
    }

    @Transactional
    public boolean cancelarTurno(Long turnoId) {
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent()) {
            Turno turno = turnoOpt.get();
            if (turno.getEstado() != Turno.EstadoTurno.CANCELADO) {
                turno.setEstado(Turno.EstadoTurno.CANCELADO);
                turnoRepository.save(turno);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean pasarTurnoPrimero(Long turnoId) {
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent() && turnoOpt.get().getEstado() == Turno.EstadoTurno.PENDIENTE) {
            Turno turno = turnoOpt.get();
            
            List<Turno> todosLosTurnosPendientes = turnoRepository.findPendientesOrdenadosPorPrioridad(
                Turno.EstadoTurno.PENDIENTE
            );
            
            java.util.OptionalInt maxPrioridadOpt = todosLosTurnosPendientes.stream()
                .filter(t -> !t.getTurnoId().equals(turnoId))
                .mapToInt(Turno::getPrioridad)
                .max();
            
            int prioridadMaxima;
            if (maxPrioridadOpt.isPresent()) {
                prioridadMaxima = maxPrioridadOpt.getAsInt();
            } else {
                prioridadMaxima = PRIORIDAD_PREFERENCIAL + 1000;
            }
            
            int nuevaPrioridad = prioridadMaxima + 50;
            turno.setPrioridad(nuevaPrioridad);
            turno.setUltimaActualizacionPrioridad(LocalDateTime.now());
            turnoRepository.save(turno);
            return true;
        }
        return false;
    }

    public Optional<Turno> obtenerTurnoActualCaja() {
        List<Turno> turnosEnAtencion = turnoRepository.findByEstado(Turno.EstadoTurno.EN_ATENCION);
        return turnosEnAtencion.stream()
            .filter(t -> t.getTipoCola() == Turno.TipoCola.CAJA)
            .findFirst();
    }

    public Optional<Turno> obtenerTurnoActualAsesoria() {
        List<Turno> turnosEnAtencion = turnoRepository.findByEstado(Turno.EstadoTurno.EN_ATENCION);
        return turnosEnAtencion.stream()
            .filter(t -> t.getTipoCola() == Turno.TipoCola.ASESORIA)
            .findFirst();
    }

    public List<Turno> obtenerTurnosPendientesPorTipoCola(Turno.TipoCola tipoCola) {
        return turnoRepository.findPendientesPorTipoCola(Turno.EstadoTurno.PENDIENTE, tipoCola);
    }

    public List<Turno> obtenerTodosLosTurnos() {
        return turnoRepository.findAllOrderedByEstadoAndFecha();
    }

    public Optional<Turno> obtenerTurnoPorId(Long turnoId) {
        return turnoRepository.findById(turnoId);
    }

    public List<Turno> obtenerTurnosPorEstado(Turno.EstadoTurno estado) {
        return turnoRepository.findByEstado(estado);
    }

    @Transactional
    public boolean eliminarTurno(Long turnoId) {
        Optional<Turno> turnoOpt = turnoRepository.findById(turnoId);
        if (turnoOpt.isPresent()) {
            Turno turno = turnoOpt.get();
            if (turno.getEstado() == Turno.EstadoTurno.CANCELADO || 
                turno.getEstado() == Turno.EstadoTurno.ATENDIDO) {
                turnoRepository.delete(turno);
                return true;
            }
        }
        return false;
    }
}
