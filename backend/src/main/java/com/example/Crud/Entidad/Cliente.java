package com.example.Crud.Entidad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false)
    private String documento;
    
    @Column(nullable = false)
    private String telefono;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaCliente categoria;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public enum CategoriaCliente {
        PREFERENCIAL,      // Embarazadas, discapacidad
        ADULTO_MAYOR,      // Adultos mayores
        VIP,               // Clientes VIP
        GENERAL            // Clientes generales
    }
}

