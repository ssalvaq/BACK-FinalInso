package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class CronogramaPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numeroPago;
    private Date fechaVencimiento;
    private double saldo;
    private double capital;
    private double interes;
    private double cuota;

    @ManyToOne
    @JoinColumn(name = "deuda_id")
    private DeudaCronogramaPago deuda;
    @Enumerated(EnumType.STRING)
    private Estado estado;
}
