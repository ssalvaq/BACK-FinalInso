package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeudaCronogramaPago extends Deuda {
    private Double tasaInteres;
    private Integer plazoMeses;

    @OneToMany(mappedBy = "deuda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CronogramaPago> cronogramaPagos;
}
