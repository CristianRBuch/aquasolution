package com.aquasolution.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @Column(name = "rol", nullable = false, length = 50)
    private String rol;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    public Auditoria() {}

    public Auditoria(String usuario, String rol, String accion, String modulo, String descripcion) {
        this.usuario = usuario;
        this.rol = rol;
        this.accion = accion;
        this.modulo = modulo;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}