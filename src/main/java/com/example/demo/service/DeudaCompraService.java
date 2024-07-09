package com.example.demo.service;

import com.example.demo.domain.dto.DeudaCompraDTO;
import com.example.demo.domain.entity.DeudaCompra;
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
public class DeudaCompraService {

    private final DeudaRepository deudaRepository; // Repositorio para manejar las operaciones CRUD de DeudaCompra
    private final UsuarioRepository usuarioRepository; // Repositorio para manejar las operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)

    /**
     * Método para registrar una nueva deuda.
     * @param deudaCompraDTO Objeto con los datos de la deuda a registrar
     * @param token Token JWT del usuario que registra la deuda
     * @return DeudaCompraDTO con los datos de la deuda registrada
     */
    public DeudaCompraDTO registrarDeuda(DeudaCompraDTO deudaCompraDTO, String token) {
        // Extraer el correo del token JWT
        String correo = jwtService.getCorreoFromToken(token);

        // Buscar el usuario en la base de datos usando el correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        // Verificar si ya existe una deuda con el mismo número de documento
        deudaRepository.findByNumeroDocumento(deudaCompraDTO.getNumeroDocumento())
                .ifPresent(existingDeuda -> {
                    throw new RuntimeException("Ya existe una deuda registrada con el número de documento: " + deudaCompraDTO.getNumeroDocumento());
                });

        // Determinar el estado de la deuda. Si no se proporciona, se asigna "PENDIENTE" por defecto.
        String estadoStr = deudaCompraDTO.getEstado();
        if (estadoStr == null || estadoStr.isEmpty()) {
            estadoStr = "PENDIENTE";
        }
        Estado estado = Estado.valueOf(estadoStr.toUpperCase()); // Convertir el estado a Enum

        // Crear una nueva instancia de DeudaCompra y asignar los valores del DTO
        DeudaCompra deudaCompra = new DeudaCompra();
        deudaCompra.setNumeroDocumento(deudaCompraDTO.getNumeroDocumento()); // Asignar número de documento
        deudaCompra.setUsuario(usuario); // Asignar el usuario obtenido
        deudaCompra.setEmpresa(deudaCompraDTO.getEmpresa()); // Asignar empresa
        deudaCompra.setMonto(deudaCompraDTO.getMonto()); // Asignar monto
        deudaCompra.setFechaVencimiento(convertToDate(deudaCompraDTO.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deudaCompra.setEstado(estado); // Asignar estado
        deudaCompra.setTipo(deudaCompraDTO.getTipo()); // Asignar tipo de deuda
        deudaCompra.setMetodoPago(deudaCompraDTO.getMetodoPago()); // Asignar método de pago

        // Guardar la entidad DeudaCompra en la base de datos
        DeudaCompra savedDeudaCompra = deudaRepository.save(deudaCompra);

        // Devolver un DTO con los datos de la deuda registrada
        return new DeudaCompraDTO(
                savedDeudaCompra.getId(), // ID de la deuda
                savedDeudaCompra.getNumeroDocumento(), // Número de documento
                savedDeudaCompra.getEmpresa(), // Empresa
                savedDeudaCompra.getMonto(), // Monto
                convertToLocalDate(savedDeudaCompra.getFechaVencimiento()), // Fecha de vencimiento
                savedDeudaCompra.getEstado().name(), // Estado
                savedDeudaCompra.getTipo(), // Tipo de deuda
                savedDeudaCompra.getMetodoPago() // Método de pago
        );
    }

    /**
     * Convertir un LocalDate a java.sql.Date
     * @param localDate Fecha en formato LocalDate
     * @return Fecha en formato java.sql.Date
     */
    private java.sql.Date convertToDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate); // Convertir y devolver
    }

    /**
     * Convertir una java.util.Date a LocalDate
     * @param date Fecha en formato java.util.Date
     * @return Fecha en formato LocalDate
     */
    private LocalDate convertToLocalDate(java.util.Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate(); // Convertir y devolver
    }
}
