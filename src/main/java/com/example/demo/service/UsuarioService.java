package com.example.demo.service;

import com.example.demo.domain.dto.UsuarioDTO;
import com.example.demo.domain.entity.Role;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.infra.repository.UsuarioRepository;
import com.example.demo.infra.security.JwtService;
import com.example.demo.infra.security.LoginRequest;
import com.example.demo.infra.security.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor // Lombok genera un constructor con los campos final
public class UsuarioService {
    private final UsuarioRepository usuarioRepository; // Repositorio para operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)
    private final PasswordEncoder passwordEncoder; // Codificador de contraseñas
    private final AuthenticationManager authenticationManager; // Administrador de autenticación

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class); // Logger para registrar información y errores

    /**
     * Método para iniciar sesión.
     * @param request Objeto que contiene las credenciales de inicio de sesión
     * @return TokenResponse con el token JWT generado
     */
    public TokenResponse login(LoginRequest request) {
        try {
            // Autenticar al usuario con el correo y contraseña proporcionados
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword()));
        } catch (Exception e) {
            // Registrar error de autenticación y lanzar excepción
            log.error("Error en la autenticación para el correo: {}", request.getCorreo(), e);
            throw new RuntimeException("Credenciales incorrectas");
        }

        // Buscar el usuario por correo
        Usuario user = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + request.getCorreo()));

        // Generar token JWT para el usuario
        String token = jwtService.getToken(user, user);
        return TokenResponse.builder().token(token).build(); // Devolver el token en la respuesta
    }

    /**
     * Método para registrar un nuevo usuario.
     * @param usuarioDTO Objeto con los datos del nuevo usuario
     * @return TokenResponse con el token JWT generado
     */
    public TokenResponse addUsuario(UsuarioDTO usuarioDTO) {
        // Crear una nueva instancia de Usuario y asignar los valores del DTO
        Usuario user = Usuario.builder()
                .username(usuarioDTO.getUsername()) // Asignar nombre de usuario
                .password(passwordEncoder.encode(usuarioDTO.getPassword())) // Codificar y asignar contraseña
                .nombre(usuarioDTO.getNombre()) // Asignar nombre
                .apellido(usuarioDTO.getApellido()) // Asignar apellido
                .telefono(usuarioDTO.getTelefono()) // Asignar teléfono
                .correo(usuarioDTO.getCorreo()) // Asignar correo
                .dni(usuarioDTO.getDni()) // Asignar DNI
                .role(Role.USER) // Asignar rol de usuario
                .build();

        // Guardar el usuario en la base de datos
        usuarioRepository.save(user);

        // Generar token JWT para el nuevo usuario
        String token = jwtService.getToken(user, user);
        return TokenResponse.builder().token(token).build(); // Devolver el token en la respuesta
    }
}
