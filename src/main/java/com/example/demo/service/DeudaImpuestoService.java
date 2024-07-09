package com.example.demo.service;

import com.example.demo.domain.dto.DeudaImpuestoDTO;
import com.example.demo.domain.entity.DeudaImpuesto;
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
public class DeudaImpuestoService {

    private final DeudaRepository deudaRepository; // Repositorio para operaciones CRUD de DeudaImpuesto
    private final UsuarioRepository usuarioRepository; // Repositorio para operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)

    /**
     * Método para registrar una nueva deuda de impuesto.
     * @param deudaImpuestoDTO Objeto con los datos de la deuda a registrar
     * @param token Token JWT del usuario que registra la deuda
     * @return DeudaImpuestoDTO con los datos de la deuda registrada
     */
    public DeudaImpuestoDTO registrarDeuda(DeudaImpuestoDTO deudaImpuestoDTO, String token) {
        // Extraer el correo del token JWT
        String correo = jwtService.getCorreoFromToken(token);

        // Buscar el usuario en la base de datos usando el correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        // Verificar si ya existe una deuda con el mismo número de documento
        deudaRepository.findByNumeroDocumento(deudaImpuestoDTO.getNumeroDocumento())
                .ifPresent(existingDeuda -> {
                    throw new RuntimeException("Ya existe una deuda registrada con el número de documento: " + deudaImpuestoDTO.getNumeroDocumento());
                });

        // Verificar y establecer el estado por defecto (PENDIENTE si no se proporciona)
        String estadoStr = deudaImpuestoDTO.getEstado();
        if (estadoStr == null || estadoStr.isEmpty()) {
            estadoStr = "PENDIENTE";
        }
        Estado estado = Estado.valueOf(estadoStr.toUpperCase()); // Convertir el estado a Enum

        // Crear una nueva instancia de DeudaImpuesto y asignar los valores del DTO
        DeudaImpuesto deudaImpuesto = new DeudaImpuesto();
        deudaImpuesto.setNumeroDocumento(deudaImpuestoDTO.getNumeroDocumento()); // Asignar número de documento
        deudaImpuesto.setUsuario(usuario); // Asignar el usuario obtenido
        deudaImpuesto.setEmpresa(deudaImpuestoDTO.getEmpresa()); // Asignar empresa
        deudaImpuesto.setMonto(deudaImpuestoDTO.getMonto()); // Asignar monto
        deudaImpuesto.setFechaVencimiento(convertToDate(deudaImpuestoDTO.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deudaImpuesto.setEstado(estado); // Asignar estado
        deudaImpuesto.setTipo(deudaImpuestoDTO.getTipo()); // Asignar tipo de deuda
        deudaImpuesto.setDetalleCobranza(deudaImpuestoDTO.getDetalleCobranza()); // Asignar detalle de cobranza

        // Guardar la entidad DeudaImpuesto en la base de datos
        DeudaImpuesto savedDeudaImpuesto = deudaRepository.save(deudaImpuesto);

        // Devolver un DTO con los datos de la deuda registrada
        return DeudaImpuestoDTO.deudaImpuestoDTOBuilder()
                .id(savedDeudaImpuesto.getId()) // Asignar ID de la deuda
                .numeroDocumento(savedDeudaImpuesto.getNumeroDocumento()) // Asignar número de documento
                .empresa(savedDeudaImpuesto.getEmpresa()) // Asignar empresa
                .monto(savedDeudaImpuesto.getMonto()) // Asignar monto
                .fechaVencimiento(convertToLocalDate(savedDeudaImpuesto.getFechaVencimiento())) // Convertir y asignar fecha de vencimiento
                .estado(savedDeudaImpuesto.getEstado().name()) // Asignar estado
                .tipo(savedDeudaImpuesto.getTipo()) // Asignar tipo de deuda
                .detalleCobranza(savedDeudaImpuesto.getDetalleCobranza()) // Asignar detalle de cobranza
                .build(); // Construir y devolver el DTO
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
