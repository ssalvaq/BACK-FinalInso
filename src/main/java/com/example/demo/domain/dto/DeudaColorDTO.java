package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DeudaColorDTO {
    private String numeroDocumento;
    private String empresa;
    private Double monto;
    private LocalDate fechaVencimiento;
    private String estado;
    private String color;
}
