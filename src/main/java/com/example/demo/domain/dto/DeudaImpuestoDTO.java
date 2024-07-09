package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaImpuestoDTO extends DeudaDTO {
    private String detalleCobranza;


    @Builder(builderMethodName = "deudaImpuestoDTOBuilder")
    public DeudaImpuestoDTO(Long id, String numeroDocumento, String empresa, Double monto, LocalDate fechaVencimiento, String estado, String tipo, String detalleCobranza, LocalDate periodo) {
        super(id, numeroDocumento, empresa, monto, fechaVencimiento, estado, tipo);
        this.detalleCobranza = detalleCobranza;

    }

    public static DeudaImpuestoDTOBuilder builder() {
        return new DeudaImpuestoDTOBuilder();
    }

    public static class DeudaImpuestoDTOBuilder extends DeudaDTO.DeudaDTOBuilder {
        private String detalleCobranza;
        private LocalDate periodo;

        DeudaImpuestoDTOBuilder() {
        }

        public DeudaImpuestoDTO.DeudaImpuestoDTOBuilder detalleCobranza(String detalleCobranza) {
            this.detalleCobranza = detalleCobranza;
            return this;
        }

        public DeudaImpuestoDTO.DeudaImpuestoDTOBuilder periodo(LocalDate periodo) {
            this.periodo = periodo;
            return this;
        }

        public DeudaImpuestoDTO build() {
            return new DeudaImpuestoDTO(this.id, this.numeroDocumento, this.empresa, this.monto, this.fechaVencimiento, this.estado, this.tipo, this.detalleCobranza, this.periodo);
        }

        public String toString() {
            return "DeudaImpuestoDTO.DeudaImpuestoDTOBuilder(detalleCobranza=" + this.detalleCobranza + ", periodo=" + this.periodo + ")";
        }
    }
}
