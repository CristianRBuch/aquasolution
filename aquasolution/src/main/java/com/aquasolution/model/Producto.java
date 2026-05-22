package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String unidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase; // Precio sin IVA

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioConIva; // Precio + 12% calculado automáticamente

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockMinimo; // Para alertas

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        calcularPrecioConIva();
    }

    @PreUpdate
    protected void onUpdate() {
        calcularPrecioConIva();
    }

    public void calcularPrecioConIva() {
        if (precioBase != null) {
            precioConIva = precioBase.multiply(new BigDecimal("1.12"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}