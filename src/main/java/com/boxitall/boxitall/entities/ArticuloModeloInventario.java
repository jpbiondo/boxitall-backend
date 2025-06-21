package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
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
    private float loteOptimo;
}
