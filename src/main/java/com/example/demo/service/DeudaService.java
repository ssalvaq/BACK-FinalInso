package com.example.demo.service;

import com.example.demo.domain.dto.DeudaDTO;
import com.example.demo.domain.dto.CronogramaPagoDTO;
import com.example.demo.domain.entity.Deuda;
import com.example.demo.domain.entity.CronogramaPago;
import com.example.demo.domain.entity.Estado;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.infra.repository.DeudaRepository;
import com.example.demo.infra.repository.CronogramaPagoRepository;
import com.example.demo.infra.repository.UsuarioRepository;
import com.example.demo.infra.security.JwtService;
import com.example.demo.web.controller.DeudaController;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeudaService {

    private final DeudaRepository deudaRepository; // Repositorio para operaciones CRUD de Deuda
    private final CronogramaPagoRepository cronogramaPagoRepository; // Repositorio para operaciones CRUD de CronogramaPago
    private final UsuarioRepository usuarioRepository; // Repositorio para operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)
    private static final Logger log = LoggerFactory.getLogger(DeudaController.class); // Logger para registrar información

    /**
     * Método para obtener el usuario desde el token JWT.
     * @param token Token JWT
     * @return Usuario obtenido del token
     */
    protected Usuario getUsuarioFromToken(String token) {
        String correo = jwtService.getCorreoFromToken(token); // Extraer el correo del token JWT
        return usuarioRepository.findByCorreo(correo) // Buscar el usuario por correo
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));
    }

    /**
     * Método para guardar una deuda.
     * @param deudaDTO Objeto con los datos de la deuda a guardar
     * @param usuario Usuario asociado a la deuda
     * @return Deuda guardada
     */
    protected Deuda saveDeuda(DeudaDTO deudaDTO, Usuario usuario) {
        Deuda deuda = new Deuda(); // Crear una nueva instancia de Deuda
        deuda.setNumeroDocumento(deudaDTO.getNumeroDocumento()); // Asignar número de documento
        deuda.setUsuario(usuario); // Asignar el usuario obtenido
        deuda.setEmpresa(deudaDTO.getEmpresa()); // Asignar empresa
        deuda.setMonto(deudaDTO.getMonto()); // Asignar monto
        deuda.setFechaVencimiento(convertToDateViaSqlDate(deudaDTO.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deuda.setEstado(Estado.valueOf(deudaDTO.getEstado())); // Asignar estado
        deuda.setTipo(deudaDTO.getTipo()); // Asignar tipo de deuda
        return deudaRepository.save(deuda); // Guardar la deuda en la base de datos y devolverla
    }

    /**
     * Método para consultar las deudas de un usuario en un mes y año específicos.
     * @param token Token JWT del usuario
     * @param month Mes para filtrar las deudas
     * @param year Año para filtrar las deudas
     * @return Lista de DeudaDTO con las deudas encontradas
     */
    public List<DeudaDTO> consultarDeudas(String token, int month, int year) {
        String correo = jwtService.getCorreoFromToken(token); // Extraer el correo del token JWT
        Usuario usuario = usuarioRepository.findByCorreo(correo) // Buscar el usuario por correo
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        LocalDate startDate = LocalDate.of(year, month, 1); // Fecha de inicio del mes
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Fecha de fin del mes

        // Buscar deudas del usuario entre las fechas especificadas
        List<Deuda> deudas = deudaRepository.findAllByUsuarioCorreoAndFechaVencimientoBetween(correo, startDate, endDate);
        // Buscar deudas anteriores pendientes del usuario
        List<Deuda> deudasAnteriores = deudaRepository.findAllByUsuarioAndFechaVencimientoBeforeAndEstado(usuario, startDate, Estado.PENDIENTE);

        deudas.addAll(deudasAnteriores); // Añadir las deudas anteriores a la lista de deudas

        // Convertir las deudas a DTOs y devolver la lista
        return deudas.stream()
                .distinct()
                .map(this::mapDeudaToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método para marcar una deuda como pagada.
     * @param deudaId ID de la deuda a marcar como pagada
     * @param token Token JWT del usuario
     * @return DeudaDTO con los datos de la deuda actualizada
     */
    public DeudaDTO marcarComoPagada(Long deudaId, String token) {
        Usuario usuario = getUsuarioFromToken(token); // Obtener el usuario desde el token JWT
        Deuda deuda = deudaRepository.findByIdAndUsuarioAndEstadoNot(deudaId, usuario, Estado.PAGADA) // Buscar la deuda por ID y usuario, excluyendo las ya pagadas
                .orElseThrow(() -> new RuntimeException("Deuda no encontrada o ya está pagada."));

        deuda.setEstado(Estado.PAGADA); // Marcar la deuda como pagada
        Deuda deudaActualizada = deudaRepository.save(deuda); // Guardar la deuda actualizada
        return mapDeudaToDTO(deudaActualizada); // Convertir y devolver la deuda actualizada a DTO
    }

    /**
     * Método para marcar un pago de cronograma como pagado.
     * @param cronogramaId ID del pago de cronograma a marcar como pagado
     * @param token Token JWT del usuario
     * @return CronogramaPagoDTO con los datos del pago actualizado
     */
    public CronogramaPagoDTO marcarPagoCronogramaComoPagado(Long cronogramaId, String token) {
        log.info("Iniciando el proceso para marcar como pagado el cronograma con ID: {}", cronogramaId);
        Usuario usuario = getUsuarioFromToken(token); // Obtener el usuario desde el token JWT
        log.info("Usuario obtenido: {}", usuario.getCorreo());

        // Buscar el pago del cronograma por ID
        CronogramaPago cronogramaPago = cronogramaPagoRepository.findById(cronogramaId)
                .orElseThrow(() -> new RuntimeException("Pago del cronograma no encontrado con el ID: " + cronogramaId));
        log.info("Cronograma de pago obtenido: {}", cronogramaPago);

        // Verificar si el pago del cronograma pertenece al usuario
        if (!cronogramaPago.getDeuda().getUsuario().equals(usuario)) {
            log.error("El pago del cronograma no pertenece al usuario. Usuario ID: {}, Deuda Usuario ID: {}",
                    usuario.getId(), cronogramaPago.getDeuda().getUsuario().getId());
            throw new RuntimeException("El pago del cronograma no pertenece al usuario.");
        }

        cronogramaPago.setEstado(Estado.PAGADA); // Marcar el pago del cronograma como pagado
        log.info("Estado del cronograma actualizado a PAGADA");

        cronogramaPagoRepository.save(cronogramaPago); // Guardar el pago del cronograma actualizado
        log.info("Cronograma de pago guardado");

        CronogramaPagoDTO dto = mapCronogramaPagoToDTO(cronogramaPago); // Convertir el pago del cronograma a DTO
        log.info("Cronograma de pago mapeado a DTO: {}", dto);

        return dto; // Devolver el DTO del pago actualizado
    }

    /**
     * Método para obtener las deudas que vencen hoy.
     * @param token Token JWT del usuario
     * @return Lista de DeudaDTO con las deudas que vencen hoy
     */
    public List<DeudaDTO> obtenerDeudasQueVencenHoy(String token) {
        String correo = jwtService.getCorreoFromToken(token); // Extraer el correo del token JWT
        Usuario usuario = usuarioRepository.findByCorreo(correo) // Buscar el usuario por correo
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        LocalDate today = LocalDate.now(); // Obtener la fecha actual

        // Buscar deudas del usuario que vencen hoy
        List<Deuda> deudas = deudaRepository.findAllByUsuarioCorreoAndFechaVencimientoAndEstado(correo, today, Estado.PENDIENTE);

        // Convertir las deudas a DTOs y devolver la lista
        return deudas.stream()
                .map(this::mapDeudaToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método para convertir una Deuda a DeudaDTO.
     * @param deuda Deuda a convertir
     * @return DeudaDTO con los datos de la deuda
     */
    private DeudaDTO mapDeudaToDTO(Deuda deuda) {
        DeudaDTO deudaDTO = new DeudaDTO();
        deudaDTO.setId(deuda.getId()); // Asignar ID de la deuda
        deudaDTO.setNumeroDocumento(deuda.getNumeroDocumento()); // Asignar número de documento
        deudaDTO.setEmpresa(deuda.getEmpresa()); // Asignar empresa
        deudaDTO.setMonto(deuda.getMonto()); // Asignar monto
        deudaDTO.setFechaVencimiento(convertToLocalDate(deuda.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deudaDTO.setEstado(deuda.getEstado().name()); // Asignar estado
        deudaDTO.setTipo(deuda.getTipo()); // Asignar tipo de deuda
        return deudaDTO; // Devolver el DTO
    }

    /**
     * Método para convertir un CronogramaPago a CronogramaPagoDTO.
     * @param cronogramaPago CronogramaPago a convertir
     * @return CronogramaPagoDTO con los datos del cronograma de pago
     */
    private CronogramaPagoDTO mapCronogramaPagoToDTO(CronogramaPago cronogramaPago) {
        return CronogramaPagoDTO.builder()
                .id(cronogramaPago.getId()) // Asignar ID del cronograma de pago
                .numeroPago(cronogramaPago.getNumeroPago()) // Asignar número de pago
                .fechaVencimiento(convertToLocalDate(cronogramaPago.getFechaVencimiento())) // Convertir y asignar fecha de vencimiento
                .saldo(cronogramaPago.getSaldo()) // Asignar saldo
                .capital(cronogramaPago.getCapital()) // Asignar capital
                .interes(cronogramaPago.getInteres()) // Asignar interés
                .cuota(cronogramaPago.getCuota()) // Asignar cuota
                .empresa(cronogramaPago.getDeuda().getEmpresa()) // Asignar empresa
                .tipoDeuda(cronogramaPago.getDeuda().getTipo()) // Asignar tipo de deuda
                .estado(cronogramaPago.getEstado().name()) // Asignar estado
                .build(); // Construir y devolver el DTO
    }

    /**
     * Convertir java.util.Date a LocalDate.
     * @param date Fecha en formato java.util.Date
     * @return Fecha en formato LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate(); // Convertir y devolver
    }

    /**
     * Convertir LocalDate a java.util.Date.
     * @param dateToConvert Fecha en formato LocalDate
     * @return Fecha en formato java.util.Date
     */
    private Date convertToDateViaSqlDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert); // Convertir y devolver
    }
}
