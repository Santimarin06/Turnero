package com.example.Crud.repository;

import com.example.Crud.Entidad.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByDocumento(String documento);
    
    List<Cliente> findByCategoria(Cliente.CategoriaCliente categoria);
    
    List<Cliente> findByActivo(Boolean activo);
    
    List<Cliente> findByNombreContaining(String nombre);
}

