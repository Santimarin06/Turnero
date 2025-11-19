package com.example.Crud.repository;

import com.example.Crud.Entidad.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurneroCajaRepository extends JpaRepository<Turno, Long> {
    
    @Query("SELECT t FROM Turno t WHERE t.estado = :estado AND t.tipoCola = 'CAJA' ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
    List<Turno> findPendientesOrdenadosPorPrioridad(@Param("estado") Turno.EstadoTurno estado);
    
    @Query("SELECT COUNT(t) FROM Turno t WHERE t.estado = :estado AND t.tipoCola = 'CAJA'")
    Long countPendientes(@Param("estado") Turno.EstadoTurno estado);
}
