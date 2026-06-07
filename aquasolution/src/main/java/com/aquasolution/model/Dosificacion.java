package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dosificaciones")
public class Dosificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroDosificacion;

    @ManyToOne
    @JoinColumn(name = "piscina_id", nullable = false)
    private Piscina piscina;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Usuario tecnico;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(nullable = false, length = 20)
    private String desinfectante;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal cloroLibre;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal ph;

    @Column(precision = 7, scale = 2)
    private BigDecimal alcalinidad;

    @Column(precision = 7, scale = 2)
    private BigDecimal calcio;

    @Column(precision = 7, scale = 2)
    private BigDecimal cya;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(nullable = false, length = 30)
    private String aspecto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenGalones;

    @Column(precision = 8, scale = 2)
    private BigDecimal dosisDesinfectante;

    @Column(precision = 8, scale = 2)
    private BigDecimal dosisPhIncreaser;

    @Column(precision = 8, scale = 2)
    private BigDecimal dosisPhDecreaser;

    @Column(precision = 8, scale = 2)
    private BigDecimal dosisAlgicida;

    @Column(precision = 8, scale = 2)
    private BigDecimal dosisClarificador;

    @Column(precision = 5, scale = 2)
    private BigDecimal lsi;

    @Column(length = 30)
    private String estadoLsi;

    @Column(length = 500)
    private String observaciones;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}