package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaCompraDTO extends DeudaDTO {
    private String metodoPago;

    public DeudaCompraDTO(Long id, String numeroDocumento, String empresa, Double monto, LocalDate fechaVencimiento, String estado, String tipo, String metodoPago) {
        super(id, numeroDocumento, empresa, monto, fechaVencimiento, estado, tipo);
        this.metodoPago = metodoPago;
    }
}
