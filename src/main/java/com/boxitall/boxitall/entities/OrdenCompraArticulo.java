package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private float cantidad ;
    @ManyToOne
    @JoinColumn
    private OrdenCompra ordenCompra;
    @ManyToOne
    @JoinColumn
    private Articulo articulo;
}
