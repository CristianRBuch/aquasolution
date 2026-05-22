package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "materiales_utilizados")
public class MaterialUtilizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporte_id", nullable = false)
    private ReporteServicio reporte;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(length = 200)
    private String notas;
}