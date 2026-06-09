package dev.osdiscretos.atlantidastore.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import dev.osdiscretos.atlantidastore.model.Usuario;

@Repository
public class UsuarioRepository {
    private final JpaUsuarioRepository jpaRepository;

    public UsuarioRepository(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Usuario save(Usuario user) {
        return jpaRepository.save(user);
    }

    public Usuario findByID(UUID id) {
        return jpaRepository.findById(id).orElse(null);
    }

    public Usuario findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return jpaRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    public List<Usuario> listAll() {
        return jpaRepository.findAll();
    }

    public void removeByID(UUID id) {
        jpaRepository.deleteById(id);
    }

    public boolean isEmailRegistered(String email) {
        if (email == null) {
            return false;
        }
        return jpaRepository.existsByEmailIgnoreCase(email);
    }
}
