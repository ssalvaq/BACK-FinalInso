package com.example.demo.infra.repository;

import com.example.demo.domain.entity.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marca esta interfaz como un repositorio de Spring
public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Long> {
    /**
     * Buscar un cronograma de pago por su ID.
     * @param id El ID del cronograma de pago
     * @return Un Optional que contiene el cronograma de pago si se encuentra
     */
    Optional<CronogramaPago> findById(Long id);
}
