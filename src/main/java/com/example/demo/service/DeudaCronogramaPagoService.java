package com.example.demo.service;

import com.example.demo.domain.dto.CronogramaPagoDTO;
import com.example.demo.domain.dto.DeudaCronogramaPagoDTO;
import com.example.demo.domain.entity.CronogramaPago;
import com.example.demo.domain.entity.DeudaCronogramaPago;
import com.example.demo.domain.entity.Estado;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.infra.repository.DeudaRepository;
import com.example.demo.infra.repository.UsuarioRepository;
import com.example.demo.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeudaCronogramaPagoService {

    private final DeudaRepository deudaRepository; // Repositorio para operaciones CRUD de DeudaCronogramaPago
    private final UsuarioRepository usuarioRepository; // Repositorio para operaciones CRUD de Usuario
    private final JwtService jwtService; // Servicio para manejar operaciones con JWT (tokens)

    /**
     * Método para registrar una nueva deuda con cronograma de pagos.
     * @param deudaCronogramaPagoDTO Objeto con los datos de la deuda a registrar
     * @param token Token JWT del usuario que registra la deuda
     * @return DeudaCronogramaPagoDTO con los datos de la deuda registrada
     */
    public DeudaCronogramaPagoDTO registrarDeuda(DeudaCronogramaPagoDTO deudaCronogramaPagoDTO, String token) {
        // Extraer el correo del token JWT
        String correo = jwtService.getCorreoFromToken(token);

        // Buscar el usuario en la base de datos usando el correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        // Verificar y establecer el estado por defecto (PENDIENTE si no se proporciona)
        String estadoStr = deudaCronogramaPagoDTO.getEstado();
        if (estadoStr == null || estadoStr.isEmpty()) {
            estadoStr = "PENDIENTE";
        }
        Estado estado = Estado.valueOf(estadoStr.toUpperCase()); // Convertir el estado a Enum

        // Crear una nueva instancia de DeudaCronogramaPago y asignar los valores del DTO
        DeudaCronogramaPago deuda = new DeudaCronogramaPago();
        deuda.setNumeroDocumento(deudaCronogramaPagoDTO.getNumeroDocumento()); // Asignar número de documento
        deuda.setUsuario(usuario); // Asignar el usuario obtenido
        deuda.setEmpresa(deudaCronogramaPagoDTO.getEmpresa()); // Asignar empresa
        deuda.setMonto(deudaCronogramaPagoDTO.getMonto()); // Asignar monto
        deuda.setFechaVencimiento(convertToDate(deudaCronogramaPagoDTO.getFechaVencimiento())); // Convertir y asignar fecha de vencimiento
        deuda.setEstado(estado); // Asignar estado
        deuda.setTipo(deudaCronogramaPagoDTO.getTipo()); // Asignar tipo de deuda
        deuda.setTasaInteres(deudaCronogramaPagoDTO.getTasaInteres()); // Asignar tasa de interés
        deuda.setPlazoMeses(deudaCronogramaPagoDTO.getPlazoMeses()); // Asignar plazo en meses

        // Crear el cronograma de pagos
        List<CronogramaPago> cronogramaPagos = new ArrayList<>();
        double saldo = deudaCronogramaPagoDTO.getMonto();
        double cuotaMensual = calcularCuotaMensual(deudaCronogramaPagoDTO.getTasaInteres(), deudaCronogramaPagoDTO.getPlazoMeses(), deudaCronogramaPagoDTO.getMonto());
        LocalDate fechaVencimiento = deudaCronogramaPagoDTO.getFechaVencimiento();

        // Crear y añadir el pago inicial al cronograma
        cronogramaPagos.add(crearPagoInicial(saldo, fechaVencimiento, deuda));

        // Crear y añadir los pagos mensuales al cronograma
        for (int i = 1; i <= deudaCronogramaPagoDTO.getPlazoMeses(); i++) {
            fechaVencimiento = fechaVencimiento.plusMonths(1); // Incrementar la fecha de vencimiento en un mes
            double interes = calcularInteres(deudaCronogramaPagoDTO.getTasaInteres(), saldo); // Calcular el interés
            double capital = calcularCapital(cuotaMensual, interes); // Calcular el capital
            saldo = calcularSaldo(saldo, capital); // Calcular el saldo

            CronogramaPago pago = new CronogramaPago();
            pago.setNumeroPago(i); // Asignar número de pago
            pago.setFechaVencimiento(convertToDate(fechaVencimiento)); // Asignar fecha de vencimiento
            pago.setSaldo(saldo); // Asignar saldo
            pago.setCapital(capital); // Asignar capital
            pago.setInteres(interes); // Asignar interés
            pago.setCuota(cuotaMensual); // Asignar cuota mensual
            pago.setDeuda(deuda); // Asignar la deuda
            pago.setEstado(Estado.PENDIENTE); // Establecer el estado por defecto como "PENDIENTE"

            cronogramaPagos.add(pago); // Añadir el pago al cronograma
        }

        deuda.setCronogramaPagos(cronogramaPagos); // Asignar el cronograma de pagos a la deuda
        DeudaCronogramaPago deudaGuardada = deudaRepository.save(deuda); // Guardar la deuda en la base de datos

        // Convertir el cronograma de pagos y devolver el DTO con los datos de la deuda registrada
        return DeudaCronogramaPagoDTO.deudaCronogramaPagoDTOBuilder()
                .id(deudaGuardada.getId())
                .numeroDocumento(deudaGuardada.getNumeroDocumento())
                .empresa(deudaGuardada.getEmpresa())
                .monto(deudaGuardada.getMonto())
                .fechaVencimiento(convertToLocalDate(deudaGuardada.getFechaVencimiento()))
                .estado(deudaGuardada.getEstado().name())
                .tipo(deudaGuardada.getTipo())
                .tasaInteres(deudaGuardada.getTasaInteres())
                .plazoMeses(deudaGuardada.getPlazoMeses())
                .cronogramaPagos(convertirCronogramaPagos(cronogramaPagos, deudaGuardada))
                .build();
    }

    /**
     * Crear el pago inicial.
     * @param saldo Saldo inicial de la deuda
     * @param fechaVencimiento Fecha de vencimiento del pago inicial
     * @param deuda Deuda a la que pertenece el pago inicial
     * @return CronogramaPago con los datos del pago inicial
     */
    private CronogramaPago crearPagoInicial(double saldo, LocalDate fechaVencimiento, DeudaCronogramaPago deuda) {
        CronogramaPago pagoInicial = new CronogramaPago();
        pagoInicial.setNumeroPago(0); // Número de pago inicial (0)
        pagoInicial.setFechaVencimiento(convertToDate(fechaVencimiento)); // Asignar fecha de vencimiento
        pagoInicial.setSaldo(saldo); // Asignar saldo inicial
        pagoInicial.setCapital(0.0); // Capital inicial (0.0)
        pagoInicial.setInteres(0.0); // Interés inicial (0.0)
        pagoInicial.setCuota(0.0); // Cuota inicial (0.0)
        pagoInicial.setDeuda(deuda); // Asignar la deuda
        pagoInicial.setEstado(Estado.PENDIENTE); // Establecer el estado por defecto como "PENDIENTE"
        return pagoInicial; // Devolver el pago inicial
    }

    /**
     * Calcular la cuota mensual.
     * @param tasaInteresAnual Tasa de interés anual
     * @param totalMeses Total de meses
     * @param montoPrestamo Monto del préstamo
     * @return Cuota mensual calculada
     */
    private double calcularCuotaMensual(double tasaInteresAnual, int totalMeses, double montoPrestamo) {
        double tasaInteresMensual = tasaInteresAnual / 12 / 100; // Convertir tasa de interés anual a mensual
        return montoPrestamo * tasaInteresMensual / (1 - Math.pow(1 + tasaInteresMensual, -totalMeses)); // Calcular y devolver la cuota mensual
    }

    /**
     * Calcular el capital en un pago.
     * @param cuota Cuota mensual
     * @param interes Interés del pago
     * @return Capital calculado
     */
    private double calcularCapital(double cuota, double interes) {
        return cuota - interes; // Calcular y devolver el capital
    }

    /**
     * Calcular el interés en un pago.
     * @param tasaInteresAnual Tasa de interés anual
     * @param saldo Saldo pendiente
     * @return Interés calculado
     */
    private double calcularInteres(double tasaInteresAnual, double saldo) {
        double tasaInteresMensual = tasaInteresAnual / 12 / 100; // Convertir tasa de interés anual a mensual
        return saldo * tasaInteresMensual; // Calcular y devolver el interés
    }

    /**
     * Calcular el saldo restante después de un pago.
     * @param saldoAnterior Saldo anterior
     * @param capital Capital pagado
     * @return Saldo restante
     */
    private double calcularSaldo(double saldoAnterior, double capital) {
        return saldoAnterior - capital; // Calcular y devolver el saldo restante
    }

    /**
     * Convertir una lista de CronogramaPago a una lista de CronogramaPagoDTO.
     * @param cronogramaPagos Lista de CronogramaPago
     * @param deuda DeudaCronogramaPago a la que pertenecen los pagos
     * @return Lista de CronogramaPagoDTO
     */
    private List<CronogramaPagoDTO> convertirCronogramaPagos(List<CronogramaPago> cronogramaPagos, DeudaCronogramaPago deuda) {
        List<CronogramaPagoDTO> listaDTO = new ArrayList<>();
        for (CronogramaPago pago : cronogramaPagos) {
            listaDTO.add(CronogramaPagoDTO.builder()
                    .id(pago.getId())
                    .numeroPago(pago.getNumeroPago())
                    .fechaVencimiento(convertToLocalDate(pago.getFechaVencimiento()))
                    .saldo(pago.getSaldo())
                    .capital(pago.getCapital())
                    .interes(pago.getInteres())
                    .cuota(pago.getCuota())
                    .empresa(deuda.getEmpresa())
                    .tipoDeuda(deuda.getTipo())
                    .estado(pago.getEstado().name())
                    .numeroDocumento(deuda.getNumeroDocumento()) // Mapear el numeroDocumento
                    .build());
        }
        return listaDTO; // Devolver la lista de DTOs
    }

    /**
     * Convertir LocalDate a java.sql.Date.
     * @param localDate Fecha en formato LocalDate
     * @return Fecha en formato java.sql.Date
     */
    private java.sql.Date convertToDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate); // Convertir y devolver
    }

    /**
     * Convertir java.util.Date a LocalDate.
     * @param date Fecha en formato java.util.Date
     * @return Fecha en formato LocalDate
     */
    private LocalDate convertToLocalDate(java.util.Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate(); // Convertir y devolver
    }

    /**
     * Obtener el cronograma de pagos del usuario autenticado.
     * @param token Token JWT del usuario
     * @return Lista de CronogramaPagoDTO
     */
    public List<CronogramaPagoDTO> obtenerCronograma(String token) {
        String correo = jwtService.getCorreoFromToken(token);
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        List<DeudaCronogramaPago> deudas = deudaRepository.findAllByUsuario(usuario);
        List<CronogramaPagoDTO> cronogramaDTOs = new ArrayList<>();
        for (DeudaCronogramaPago deuda : deudas) {
            List<CronogramaPago> cronograma = deuda.getCronogramaPagos();
            cronogramaDTOs.addAll(convertirCronogramaPagos(cronograma, deuda));
        }
        return cronogramaDTOs;
    }

    /**
     * Marcar un pago de cronograma como pagado.
     * @param id ID del pago a marcar como pagado
     * @param token Token JWT del usuario
     * @return CronogramaPagoDTO con los datos del pago actualizado
     */
    public CronogramaPagoDTO marcarPagoCronogramaComoPagado(Long id, String token) {
        String correo = jwtService.getCorreoFromToken(token);
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));

        CronogramaPago cronogramaPago = deudaRepository.findCronogramaPagoByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new RuntimeException("Cronograma de pago no encontrado para el usuario con el ID: " + id));

        cronogramaPago.setEstado(Estado.PAGADA);
        deudaRepository.save(cronogramaPago.getDeuda()); // Actualiza la deuda con el cronograma modificado

        return CronogramaPagoDTO.builder()
                .numeroPago(cronogramaPago.getNumeroPago())
                .fechaVencimiento(convertToLocalDate(cronogramaPago.getFechaVencimiento()))
                .saldo(cronogramaPago.getSaldo())
                .capital(cronogramaPago.getCapital())
                .interes(cronogramaPago.getInteres())
                .cuota(cronogramaPago.getCuota())
                .empresa(cronogramaPago.getDeuda().getEmpresa())
                .tipoDeuda(cronogramaPago.getDeuda().getTipo())
                .estado(cronogramaPago.getEstado().name())
                .build();
    }
}
