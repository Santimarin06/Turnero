package com.example.Crud.repository;

import com.example.Crud.Entidad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByNombreContaining(String nombre);
    List<Usuario> findByRol(Usuario.Rol rol);
    List<Usuario> findByEstado(Usuario.Estado estado);
}
