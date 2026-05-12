package com.malcom.sm4rtstock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "movimientos",
        indexes = {
                @Index(name = "idx_movimiento_fecha", columnList = "fecha"),
                @Index(name = "idx_movimiento_tipo_fecha", columnList = "tipo,fecha"),
                @Index(name = "idx_movimiento_producto_fecha", columnList = "producto_id,fecha"),
                @Index(name = "idx_movimiento_usuario_fecha", columnList = "usuario_id,fecha")
        }
)
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

    @Column(length = 255)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCrear() {
        this.fecha = LocalDateTime.now();
    }
}
