package com.example.Crud.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long turnoId;
    
    @Column(name = "numero_turno", nullable = false, unique = true)
    private String numeroTurno;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTurno estado;
    
    @Column(name = "prioridad", nullable = false)
    private Integer prioridad;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;
    
    @Column(name = "tiempo_espera_minutos")
    private Long tiempoEsperaMinutos;
    
    @Column(name = "ultima_actualizacion_prioridad")
    private LocalDateTime ultimaActualizacionPrioridad;
    
    @Column(name = "contador_aging")
    private Integer contadorAging = 0;
    
    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;
    
    @Column(name = "tipo_cola", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCola tipoCola;

    public enum EstadoTurno {
        PENDIENTE,
        EN_ATENCION,
        ATENDIDO,
        CANCELADO       
    }
    
    public enum TipoCola {
        CAJA,
        ASESORIA
    }
}

