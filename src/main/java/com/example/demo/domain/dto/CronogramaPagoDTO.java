package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CronogramaPagoDTO {
    private Long id;  // Agrega este campo
    private int numeroPago;
    private LocalDate fechaVencimiento;
    private double saldo;
    private double capital;
    private double interes;
    private double cuota;
    private String empresa;
    private String tipoDeuda;
    private String estado;
    private String numeroDocumento;
}
