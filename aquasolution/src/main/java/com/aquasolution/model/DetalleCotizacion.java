package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalles_cotizacion")
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto; // Opcional — puede ser producto del catálogo o manual

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(length = 50)
    private String unidad;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario; // Precio con IVA incluido

    @Column(precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO; // % descuento 0-100

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // (precioUnitario * cantidad) - descuento
}