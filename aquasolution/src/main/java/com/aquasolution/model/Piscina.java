package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "piscinas")
public class Piscina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPiscina tipo;

    @Column(nullable = false)
    private Double largo;

    @Column
    private Double ancho;

    @Column(nullable = false)
    private Double profundidadPromedio;

    @Column(nullable = false)
    private Double volumen;

    @Column(length = 200)
    private String ubicacion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    public enum TipoPiscina {
        RECTANGULAR, CIRCULAR, OVAL, RINON, IRREGULAR
    }
}