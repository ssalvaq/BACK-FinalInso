package com.example.demo.domain.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeudaCompra extends Deuda {
    private String numeroFactura;
    private Date fechaCompra;
    private String metodoPago;
}
