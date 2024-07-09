package com.example.demo.infra.repository;

import com.example.demo.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository // Marca esta interfaz como un repositorio de Spring
public interface DeudaRepository extends JpaRepository<Deuda, Long> {
    /**
     * Buscar una deuda por número de documento.
     * @param numeroDocumento El número de documento de la deuda
     * @return Un Optional que contiene la deuda si se encuentra
     */
    Optional<Deuda> findByNumeroDocumento(String numeroDocumento);

    /**
     * Buscar todas las deudas de un usuario por su correo y entre dos fechas de vencimiento.
     * @param correo El correo del usuario
     * @param startDate La fecha de inicio del rango
     * @param endDate La fecha de fin del rango
     * @return Una lista de deudas que cumplen con los criterios de búsqueda
     */
    List<Deuda> findAllByUsuarioCorreoAndFechaVencimientoBetween(String correo, LocalDate startDate, LocalDate endDate);

    /**
     * Buscar todas las deudas de un usuario antes de una fecha específica y con un estado determinado.
     * @param usuario El usuario al que pertenecen las deudas
     * @param date La fecha límite para buscar deudas
     * @param estado El estado de las deudas a buscar
     * @return Una lista de deudas que cumplen con los criterios de búsqueda
     */
    List<Deuda> findAllByUsuarioAndFechaVencimientoBeforeAndEstado(Usuario usuario, LocalDate date, Estado estado);

    /**
     * Buscar una deuda por su ID, usuario y un estado diferente al proporcionado.
     * @param id El ID de la deuda
     * @param usuario El usuario al que pertenece la deuda
     * @param estado El estado que la deuda no debe tener
     * @return Un Optional que contiene la deuda si se encuentra
     */
    Optional<Deuda> findByIdAndUsuarioAndEstadoNot(Long id, Usuario usuario, Estado estado);

    /**
     * Buscar todas las deudas cronograma de pago de un usuario.
     * @param usuario El usuario al que pertenecen las deudas cronograma de pago
     * @return Una lista de deudas cronograma de pago que cumplen con los criterios de búsqueda
     */
    List<DeudaCronogramaPago> findAllByUsuario(Usuario usuario);

    /**
     * Buscar todas las deudas de un usuario por su correo, una fecha de vencimiento específica y un estado determinado.
     * @param correo El correo del usuario
     * @param fechaVencimiento La fecha de vencimiento de las deudas a buscar
     * @param estado El estado de las deudas a buscar
     * @return Una lista de deudas que cumplen con los criterios de búsqueda
     */
    List<Deuda> findAllByUsuarioCorreoAndFechaVencimientoAndEstado(String correo, LocalDate fechaVencimiento, Estado estado);

    /**
     * Buscar un cronograma de pago por su ID y usuario.
     * @param id El ID del cronograma de pago
     * @param usuario El usuario al que pertenece el cronograma de pago
     * @return Un Optional que contiene el cronograma de pago si se encuentra
     */
    Optional<CronogramaPago> findCronogramaPagoByIdAndUsuario(Long id, Usuario usuario);
}
