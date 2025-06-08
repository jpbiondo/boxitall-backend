package com.boxitall.boxitall.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table (name = "venta_detalles")
public class VentaDetalle extends BaseEntity{
    @Column(nullable = false)
    private float cantidad;

    @Column(nullable = false)
    private int renglon;

    //Relaciones
    @ManyToOne (optional = false, cascade = CascadeType.REFRESH)
    private Articulo articulo;
}
