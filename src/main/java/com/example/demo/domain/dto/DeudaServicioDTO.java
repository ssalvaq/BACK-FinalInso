package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaServicioDTO extends DeudaDTO {
    private String referenciaServicio;

    public DeudaServicioDTO(Long id, String numeroDocumento, String empresa, Double monto, LocalDate fechaVencimiento, String estado, String tipo, String referenciaServicio) {
        super(id, numeroDocumento, empresa, monto, fechaVencimiento, estado, tipo);
        this.referenciaServicio = referenciaServicio;
    }
}
