package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeudaCronogramaPagoDTO extends DeudaDTO {
    private List<CronogramaPagoDTO> cronogramaPagos;
    private Double tasaInteres;
    private Integer plazoMeses;

    @Builder(builderMethodName = "deudaCronogramaPagoDTOBuilder")
    public DeudaCronogramaPagoDTO(Long id, String numeroDocumento, String empresa, Double monto, LocalDate fechaVencimiento, String estado, String tipo, List<CronogramaPagoDTO> cronogramaPagos, Double tasaInteres, Integer plazoMeses) {
        super(id, numeroDocumento, empresa, monto, fechaVencimiento, estado, tipo);
        this.cronogramaPagos = cronogramaPagos;
        this.tasaInteres = tasaInteres;
        this.plazoMeses = plazoMeses;
    }

    public static class DeudaCronogramaPagoDTOBuilder extends DeudaDTOBuilder {
        private List<CronogramaPagoDTO> cronogramaPagos;
        private Double tasaInteres;
        private Integer plazoMeses;

        public DeudaCronogramaPagoDTOBuilder cronogramaPagos(List<CronogramaPagoDTO> cronogramaPagos) {
            this.cronogramaPagos = cronogramaPagos;
            return this;
        }

        public DeudaCronogramaPagoDTOBuilder tasaInteres(Double tasaInteres) {
            this.tasaInteres = tasaInteres;
            return this;
        }

        public DeudaCronogramaPagoDTOBuilder plazoMeses(Integer plazoMeses) {
            this.plazoMeses = plazoMeses;
            return this;
        }

        @Override
        public DeudaCronogramaPagoDTO build() {
            return new DeudaCronogramaPagoDTO(id, numeroDocumento, empresa, monto, fechaVencimiento, estado, tipo, cronogramaPagos, tasaInteres, plazoMeses);
        }
    }
}
