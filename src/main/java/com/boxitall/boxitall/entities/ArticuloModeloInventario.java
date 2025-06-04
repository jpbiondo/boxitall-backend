package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@NoArgsConstructor
public abstract class ArticuloModeloInventario extends BaseEntity{
    private float stockSeguridad;

    @Transient
    private String nombre; //Nombre del modelo de inventario
}
