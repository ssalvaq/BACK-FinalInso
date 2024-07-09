package com.example.demo.domain.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class DeudaServicio extends Deuda {
    private String referenciaServicio;
}
