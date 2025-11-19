package com.example.Crud.Service;

import com.example.Crud.Entidad.Cliente;
import com.example.Crud.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> getAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> getById(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> getByDocumento(String documento) {
        return clienteRepository.findByDocumento(documento);
    }

    public List<Cliente> getByCategoria(Cliente.CategoriaCliente categoria) {
        return clienteRepository.findByCategoria(categoria);
    }

    public boolean create(Cliente cliente) {
        if (clienteRepository.findByDocumento(cliente.getDocumento()).isEmpty()) {
            cliente.setFechaRegistro(LocalDateTime.now());
            cliente.setActivo(true);
            clienteRepository.save(cliente);
            return true;
        }
        return false;
    }

    public boolean update(Cliente cliente) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(cliente.getClienteId());
        if (clienteOpt.isPresent()) {
            Cliente clienteExistente = clienteOpt.get();
            
            Optional<Cliente> clienteConDocumento = clienteRepository.findByDocumento(cliente.getDocumento());
            if (clienteConDocumento.isEmpty() || clienteConDocumento.get().getClienteId().equals(cliente.getClienteId())) {
                cliente.setFechaRegistro(clienteExistente.getFechaRegistro());
                if (cliente.getActivo() == null) {
                    cliente.setActivo(clienteExistente.getActivo());
                }
                clienteRepository.save(cliente);
                return true;
            }
        }
        return false;
    }

    public String delete(Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        if (cliente.isPresent()) {
            cliente.get().setActivo(false);
            clienteRepository.save(cliente.get());
            return "Cliente desactivado correctamente";
        }
        return "Error: Cliente no encontrado";
    }
}
