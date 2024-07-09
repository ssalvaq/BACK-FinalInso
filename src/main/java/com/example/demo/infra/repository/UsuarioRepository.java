package com.example.demo.infra.repository;

import com.example.demo.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Buscar un usuario por su correo electrónico.
     * @param correo El correo electrónico del usuario
     * @return Un Optional que contiene el usuario si se encuentra
     */
    Optional<Usuario> findByCorreo(String correo);
}
