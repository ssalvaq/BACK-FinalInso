package com.example.demo.service;

import com.example.demo.domain.dto.DeudaServicioDTO;
import com.example.demo.domain.entity.DeudaServicio;
import com.example.demo.domain.entity.Estado;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.infra.repository.DeudaRepository;
import com.example.demo.infra.repository.UsuarioRepository;
import com.example.demo.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor // Lombok genera un constructor con los campos final
public class DeudaServicioService {

    private final DeudaRepository deudaRepository; // Repositorio para operaciones CRUD de DeudaServicio
    private final UsuarioRepository usuarioRepository; // Repositorio para operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)

    /**
     * Método para registrar una nueva deuda de servicio.
     * @param deudaServicioDTO Objeto con los datos de la deuda a registrar
     * @param token Token JWT del usuario que registra la deuda
     * @return DeudaServicioDTO con los datos de la deuda registrada
     */
    public DeudaServicioDTO registrarDeuda(DeudaServicioDTO deudaServicioDTO, String token) {
        // Extraer el correo del token JWT
        String correo = jwtService.getCorreoFromToken(token);

        // Buscar el usuario en la base de datos usando el correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        // Verificar si ya existe una deuda con el mismo número de documento
        deudaRepository.findByNumeroDocumento(deudaServicioDTO.getNumeroDocumento())
                .ifPresent(existingDeuda -> {
                    throw new RuntimeException("Ya existe una deuda registrada con el número de documento: " + deudaServicioDTO.getNumeroDocumento());
                });

        // Verificar y establecer el estado por defecto (PENDIENTE si no se proporciona)
        String estadoStr = deudaServicioDTO.getEstado();
        if (estadoStr == null || estadoStr.isEmpty()) {
            estadoStr = "PENDIENTE";
        }
        Estado estado = Estado.valueOf(estadoStr.toUpperCase()); // Convertir el estado a Enum

        // Crear una nueva instancia de DeudaServicio y asignar los valores del DTO
        DeudaServicio deudaServicio = new DeudaServicio();
        deudaServicio.setNumeroDocumento(deudaServicioDTO.getNumeroDocumento()); // Asignar número de documento
        deudaServicio.setUsuario(usuario); // Asignar el usuario obtenido
        deudaServicio.setEmpresa(deudaServicioDTO.getEmpresa()); // Asignar empresa
        deudaServicio.setMonto(deudaServicioDTO.getMonto()); // Asignar monto
        deudaServicio.setFechaVencimiento(convertToDate(deudaServicioDTO.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deudaServicio.setEstado(estado); // Asignar estado
        deudaServicio.setTipo(deudaServicioDTO.getTipo()); // Asignar tipo de deuda
        deudaServicio.setReferenciaServicio(deudaServicioDTO.getReferenciaServicio()); // Asignar referencia de servicio

        // Guardar la entidad DeudaServicio en la base de datos
        DeudaServicio savedDeudaServicio = deudaRepository.save(deudaServicio);

        // Devolver un DTO con los datos de la deuda registrada
        return new DeudaServicioDTO(
                savedDeudaServicio.getId(), // Asignar ID de la deuda
                savedDeudaServicio.getNumeroDocumento(), // Asignar número de documento
                savedDeudaServicio.getEmpresa(), // Asignar empresa
                savedDeudaServicio.getMonto(), // Asignar monto
                convertToLocalDate(savedDeudaServicio.getFechaVencimiento()), // Convertir y asignar fecha de vencimiento
                savedDeudaServicio.getEstado().name(), // Asignar estado
                savedDeudaServicio.getTipo(), // Asignar tipo de deuda
                savedDeudaServicio.getReferenciaServicio() // Asignar referencia de servicio
        );
    }

    /**
     * Convertir LocalDate a java.sql.Date
     * @param localDate Fecha en formato LocalDate
     * @return Fecha en formato java.sql.Date
     */
    private java.sql.Date convertToDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate); // Convertir y devolver
    }

    /**
     * Convertir java.util.Date a LocalDate
     * @param date Fecha en formato java.util.Date
     * @return Fecha en formato LocalDate
     */
    private LocalDate convertToLocalDate(java.util.Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate(); // Convertir y devolver
    }
}
