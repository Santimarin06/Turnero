package com.example.Crud.controller;

import com.example.Crud.Entidad.Cliente;
import com.example.Crud.Service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/obtener-todos")
    public List<Cliente> getAll() {
        return clienteService.getAll();
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<Cliente> getById(@PathVariable("clienteId") Long clienteId) {
        Optional<Cliente> cliente = clienteService.getById(clienteId);
        return cliente.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<Cliente> getByDocumento(@PathVariable("documento") String documento) {
        Optional<Cliente> cliente = clienteService.getByDocumento(documento);
        return cliente.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoria}")
    public List<Cliente> getByCategoria(@PathVariable("categoria") String categoria) {
        try {
            Cliente.CategoriaCliente categoriaEnum = Cliente.CategoriaCliente.valueOf(categoria.toUpperCase());
            return clienteService.getByCategoria(categoriaEnum);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Cliente cliente) {
        if (clienteService.create(cliente)) {
            return ResponseEntity.ok("Cliente guardado correctamente");
        }
        return ResponseEntity.badRequest().body("Error al guardar el cliente: el documento ya existe");
    }

    @PutMapping
    public ResponseEntity<String> update(@RequestBody Cliente cliente) {
        if (clienteService.update(cliente)) {
            return ResponseEntity.ok("Cliente actualizado correctamente");
        }
        return ResponseEntity.badRequest().body("Error al actualizar el cliente: no se encontró o el documento ya existe");
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<String> delete(@PathVariable("clienteId") Long clienteId) {
        String resultado = clienteService.delete(clienteId);
        if (resultado.contains("correctamente")) {
            return ResponseEntity.ok(resultado);
        }
        return ResponseEntity.badRequest().body(resultado);
    }
}

