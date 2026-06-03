package dev.osdiscretos.atlantidastore.repository;

import java.util.UUID;
import java.util.List;

import dev.osdiscretos.atlantidastore.model.Usuario;

public interface UsuarioRepository {
    Usuario save(Usuario user);

    Usuario findByID(UUID id);

    Usuario findByEmail(String email);

    List<Usuario> listAll();

    void removeByID(UUID id);

    boolean isEmailRegistered(String email);
}
