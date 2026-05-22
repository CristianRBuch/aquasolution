package com.aquasolution.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "reportes_servicio")
public class ReporteServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoServicio tipoServicio;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Usuario tecnico;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "piscina_id")
    private Piscina piscina;

    @Column(nullable = false)
    private LocalDateTime fechaServicio;

    // Campos Piscina/Jacuzzi
    @Column(length = 100)
    private String bombaPiscina;

    @Column(length = 100)
    private String filtroTipo;

    @Column(length = 50)
    private String filtroTamanio;

    @Column(length = 50)
    private String lamparas;

    @Column(length = 100)
    private String calentador;

    @Column(length = 50)
    private String cantidadValvulas;

    @Column(length = 50)
    private String diametroValvulas;

    // Campos Hidroneumático
    @Column(length = 100)
    private String bombaHidro;

    @Column(length = 100)
    private String tanqueHidroneumatico;

    @Column(length = 20)
    private String voltajeHidro;

    @Column(length = 20)
    private String amperajeHidro;

    // Campos Pozo
    @Column(length = 50)
    private String diametroPozo;

    @Column(length = 50)
    private String diametroTuberia;

    @Column(length = 50)
    private String cantidadTubos;

    @Column(length = 100)
    private String potenciaBomba;

    @Column(length = 100)
    private String potenciaMotor;

    @Column(length = 50)
    private String tipoFase;

    @Column(length = 20)
    private String voltajePozo;

    @Column(length = 50)
    private String calibreCable;

    // Trabajo realizado
    @Column(length = 1000)
    private String trabajoRealizado;

    @Column(length = 500)
    private String proximaVisita;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialUtilizado> materiales;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public enum TipoServicio {
        PISCINA_JACUZZI,
        HIDRONEUMATICO,
        POZO,
        BOMBA_GENERAL,
        RIEGO,
        OTRO
    }
}