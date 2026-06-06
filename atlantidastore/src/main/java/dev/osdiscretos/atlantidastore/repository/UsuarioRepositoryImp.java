package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImp implements UsuarioRepository {
    private final JpaUsuarioRepository jpaRepository;

    public UsuarioRepositoryImp(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario save(Usuario user) {
        return jpaRepository.save(user);
    }

    @Override
    public Usuario findByID(UUID id) {
        return jpaRepository.findById(id).orElse(null);
    }

    @Override
    public Usuario findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return jpaRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    @Override
    public List<Usuario> listAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void removeByID(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        if (email == null) {
            return false;
        }
        return jpaRepository.existsByEmailIgnoreCase(email);
    }
}
