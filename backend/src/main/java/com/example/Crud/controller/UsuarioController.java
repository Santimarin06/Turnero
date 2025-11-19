package com.example.Crud.controller;

import com.example.Crud.Entidad.Usuario;
import com.example.Crud.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "api/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("obtener-todos")
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public Optional<Usuario> getById(@PathVariable("id") Long id) {
        return usuarioService.getById(id);
    }

    @PostMapping
    public String create(@RequestBody Usuario usuario) {
        if (usuarioService.create(usuario)) {
            return "Usuario creado correctamente";
        }
        return "Error: el correo o username ya existe";
    }

    @PutMapping
    public String update(@RequestBody Usuario usuario) {
        if (usuarioService.update(usuario)) {
            return "Usuario actualizado correctamente";
        }
        return "Error: no se encontró el usuario o el correo/username ya existe";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long id) {
        return usuarioService.delete(id);
    }
}
