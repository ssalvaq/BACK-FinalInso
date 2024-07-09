package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(callSuper = false)
public class Deuda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroDocumento;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String empresa;
    private Double monto;

    @Temporal(TemporalType.DATE)
    private Date fechaVencimiento;

    @Enumerated(EnumType.STRING)
    private Estado estado;
    private String tipo;

    @OneToMany(mappedBy = "deuda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleDeuda> detalle;
}
