package com.boxitall.boxitall.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdenCompraArticulo extends BaseEntity {
    @Column(nullable = false)
    private float cantidad;

    @Column(nullable = false)
    private int renglon;

    @ManyToOne (optional = false, cascade = CascadeType.REFRESH)
    private Articulo articulo;

}
