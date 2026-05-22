package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "cotizaciones")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroCotizacion;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Usuario tecnico;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCotizacion> detalles;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuentoGlobal = BigDecimal.ZERO; // % descuento global opcional

    @Column(precision = 10, scale = 2)
    private BigDecimal totalConDescuento; // Total después de descuento global

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado;

    @Column(length = 500)
    private String observaciones;

    // Campos editables de condiciones
    @Column(length = 200)
    private String validezOferta;

    @Column(length = 200)
    private String tiempoEntrega;

    @Column(length = 200)
    private String condicionesPago;

    @Column(length = 200)
    private String garantia;

    @Column(length = 500)
    private String notaPrecios;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        estado = EstadoCotizacion.PENDIENTE;
    }

    public enum EstadoCotizacion {
        PENDIENTE, APROBADA, RECHAZADA, VENCIDA
    }
}