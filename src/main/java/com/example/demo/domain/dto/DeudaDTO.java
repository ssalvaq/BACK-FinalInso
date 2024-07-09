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
public class DeudaDTO {
    private Long id;
    private String numeroDocumento;
    private String empresa;
    private Double monto;
    private LocalDate fechaVencimiento;
    private String estado = "PENDIENTE";
    private String tipo;

    public static class DeudaDTOBuilder {
        public DeudaDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }
        public DeudaDTOBuilder numeroDocumento(String numeroDocumento) {
            this.numeroDocumento = numeroDocumento;
            return this;
        }
        public DeudaDTOBuilder empresa(String empresa) {
            this.empresa = empresa;
            return this;
        }
        public DeudaDTOBuilder monto(Double monto) {
            this.monto = monto;
            return this;
        }
        public DeudaDTOBuilder fechaVencimiento(LocalDate fechaVencimiento) {
            this.fechaVencimiento = fechaVencimiento;
            return this;
        }
        public DeudaDTOBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }
        public DeudaDTOBuilder tipo(String tipo) {
            this.tipo = tipo;
            return this;
        }
    }
}
