package com.example.Crud.Entidad;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuarioId;
    private String nombre;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    public enum Rol {
        ADMIN, CAJERO, ASESOR
    }

    public enum Estado {
        ACTIVO, INACTIVO
    }
}
