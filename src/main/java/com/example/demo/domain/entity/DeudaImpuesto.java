package com.example.demo.domain.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeudaImpuesto extends Deuda {
    private String detalleCobranza;
    private Date periodo;
}
