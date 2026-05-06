package com.malcom.sm4rtstock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore // CRÍTICO: Evita que el historial entre en un bucle infinito al generar el JSON
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER para asegurar que el usuario llegue al frontend
    @JoinColumn(name = "usuario_id", nullable = true)
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer stockAnterior;

    @Column(nullable = false)
    private Integer stockNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCrear() {
        this.fecha = LocalDateTime.now();
    }
}