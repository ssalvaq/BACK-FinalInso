package com.example.demo.web.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Marca esta clase como un controlador REST de Spring
@RequestMapping("/deudas") // Define la ruta base para las solicitudes manejadas por este controlador
@RequiredArgsConstructor // Lombok genera un constructor con los campos final
public class DeudaController {

    // Inyección de dependencias de los servicios
    private final DeudaCompraService deudaCompraService;
    private final DeudaServicioService deudaServicioService;
    private final DeudaImpuestoService deudaImpuestoService;
    private final DeudaCronogramaPagoService deudaCronogramaPagoService;
    private final DeudaService deudaService;

    /**
     * Registrar una nueva deuda de compra.
     * @param deudaCompraDTO Objeto con los datos de la deuda de compra
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return DeudaCompraDTO con los datos de la deuda registrada
     */
    @PostMapping("/registrar/compra")
    public DeudaCompraDTO registrarDeudaCompra(@RequestBody DeudaCompraDTO deudaCompraDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaCompraService.registrarDeuda(deudaCompraDTO, token);
    }

    /**
     * Registrar una nueva deuda de servicio.
     * @param deudaServicioDTO Objeto con los datos de la deuda de servicio
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return DeudaServicioDTO con los datos de la deuda registrada
     */
    @PostMapping("/registrar/servicio")
    public DeudaServicioDTO registrarDeudaServicio(@RequestBody DeudaServicioDTO deudaServicioDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaServicioService.registrarDeuda(deudaServicioDTO, token);
    }

    /**
     * Registrar una nueva deuda de impuesto.
     * @param deudaImpuestoDTO Objeto con los datos de la deuda de impuesto
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return DeudaImpuestoDTO con los datos de la deuda registrada
     */
    @PostMapping("/registrar/impuesto")
    public DeudaImpuestoDTO registrarDeudaImpuesto(@RequestBody DeudaImpuestoDTO deudaImpuestoDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaImpuestoService.registrarDeuda(deudaImpuestoDTO, token);
    }

    /**
     * Registrar una nueva deuda con cronograma de pagos.
     * @param deudaCronogramaPagoDTO Objeto con los datos de la deuda y el cronograma de pagos
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return ResponseEntity con los datos de la deuda registrada
     */
    @PostMapping("/registrar/cronograma")
    public ResponseEntity<DeudaCronogramaPagoDTO> registrarDeudaCronograma(@RequestBody DeudaCronogramaPagoDTO deudaCronogramaPagoDTO, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        DeudaCronogramaPagoDTO deudaRegistrada = deudaCronogramaPagoService.registrarDeuda(deudaCronogramaPagoDTO, token);
        return ResponseEntity.ok(deudaRegistrada);
    }

    /**
     * Consultar las deudas del usuario para un mes y año específicos.
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @param month Mes para filtrar las deudas
     * @param year Año para filtrar las deudas
     * @return Lista de DeudaDTO con las deudas encontradas
     */
    @GetMapping("/consultar")
    public List<DeudaDTO> consultarDeudas(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                          @RequestParam int month,
                                          @RequestParam int year) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaService.consultarDeudas(token, month, year);
    }

    /**
     * Marcar una deuda como pagada.
     * @param id ID de la deuda a marcar como pagada
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return DeudaDTO con los datos de la deuda actualizada
     */
    @PostMapping("/marcar-pagada/{id}")
    public DeudaDTO marcarDeudaComoPagada(@PathVariable Long id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaService.marcarComoPagada(id, token);
    }

    /**
     * Obtener el cronograma de pagos del usuario autenticado.
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return Lista de CronogramaPagoDTO con el cronograma de pagos
     */
    @GetMapping("/cronograma")
    public List<CronogramaPagoDTO> obtenerCronograma(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaCronogramaPagoService.obtenerCronograma(token);
    }

    /**
     * Obtener las deudas que vencen hoy.
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return Lista de DeudaDTO con las deudas que vencen hoy
     */
    @GetMapping("/vencen-hoy")
    public List<DeudaDTO> obtenerDeudasQueVencenHoy(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        return deudaService.obtenerDeudasQueVencenHoy(token);
    }

    /**
     * Marcar un pago de cronograma como pagado.
     * @param id ID del pago de cronograma a marcar como pagado
     * @param authHeader Cabecera de autorización que contiene el token JWT
     * @return ResponseEntity con los datos del pago actualizado
     */
    @PostMapping("/marcar-pagada-cronograma/{id}")
    public ResponseEntity<CronogramaPagoDTO> marcarPagoCronogramaComoPagado(@PathVariable Long id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.substring(7); // Remover "Bearer " del token
        CronogramaPagoDTO cronogramaPagoDTO = deudaService.marcarPagoCronogramaComoPagado(id, token);
        return ResponseEntity.ok(cronogramaPagoDTO);
    }
}
