package com.example.Crud.repository;

import com.example.Crud.Entidad.Turno;
import com.example.Crud.Entidad.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    Optional<Turno> findByNumeroTurno(String numeroTurno);
    
    List<Turno> findByCliente(Cliente cliente);
    
    List<Turno> findByEstado(Turno.EstadoTurno estado);
    
    List<Turno> findByClienteCategoria(Cliente.CategoriaCliente categoria);
    
    @Query("SELECT t FROM Turno t WHERE t.estado = :estado ORDER BY t.prioridad DESC, t.fechaCreacion ASC, t.turnoId ASC")
    List<Turno> findPendientesOrdenadosPorPrioridad(@Param("estado") Turno.EstadoTurno estado);
    
    @Query("SELECT t FROM Turno t WHERE t.estado = :estado AND t.cliente.categoria = :categoria AND " +
           "(t.ultimaActualizacionPrioridad IS NULL OR t.ultimaActualizacionPrioridad <= :fechaLimite)")
    List<Turno> findTurnosParaAging(@Param("estado") Turno.EstadoTurno estado, 
                                     @Param("categoria") Cliente.CategoriaCliente categoria,
                                     @Param("fechaLimite") LocalDateTime fechaLimite);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.estado = :estado AND t.cliente.categoria = :categoria")
    Long countByEstadoAndCategoria(@Param("estado") Turno.EstadoTurno estado, 
                                    @Param("categoria") Cliente.CategoriaCliente categoria);
    
    @Query("SELECT t FROM Turno t WHERE t.estado = :estado AND t.tipoCola = :tipoCola ORDER BY t.prioridad DESC, t.fechaCreacion ASC, t.turnoId ASC")
    List<Turno> findPendientesPorTipoCola(@Param("estado") Turno.EstadoTurno estado, 
                                           @Param("tipoCola") Turno.TipoCola tipoCola);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.estado = :estado AND t.tipoCola = :tipoCola")
    Long countByEstadoAndTipoCola(@Param("estado") Turno.EstadoTurno estado, 
                                   @Param("tipoCola") Turno.TipoCola tipoCola);
    
    @Query("SELECT t FROM Turno t WHERE t.estado = :estado AND t.tipoCola = :tipoCola AND t.cliente.categoria = :categoria AND " +
           "(t.ultimaActualizacionPrioridad IS NULL OR t.ultimaActualizacionPrioridad <= :fechaLimite)")
    List<Turno> findTurnosParaAgingPorTipoCola(@Param("estado") Turno.EstadoTurno estado, 
                                                 @Param("tipoCola") Turno.TipoCola tipoCola,
                                                 @Param("categoria") Cliente.CategoriaCliente categoria,
                                                 @Param("fechaLimite") LocalDateTime fechaLimite);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.cliente.categoria = :categoria AND t.tipoCola = :tipoCola")
    Long countByCategoriaAndTipoCola(@Param("categoria") Cliente.CategoriaCliente categoria,
                                     @Param("tipoCola") Turno.TipoCola tipoCola);
    
    @Query("SELECT t FROM Turno t WHERE t.cliente.clienteId = :clienteId AND t.estado = :estado")
    List<Turno> findByClienteIdAndEstado(@Param("clienteId") Long clienteId, 
                                          @Param("estado") Turno.EstadoTurno estado);
    
    @Query("SELECT t FROM Turno t ORDER BY " +
           "CASE t.estado " +
           "WHEN 'EN_ATENCION' THEN 1 " +
           "WHEN 'PENDIENTE' THEN 2 " +
           "WHEN 'ATENDIDO' THEN 3 " +
           "WHEN 'CANCELADO' THEN 4 " +
           "ELSE 5 END, " +
           "t.prioridad DESC, " +
           "t.fechaCreacion ASC, " +
           "t.turnoId ASC")
    List<Turno> findAllOrderedByEstadoAndFecha();
}
