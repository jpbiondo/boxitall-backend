package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ArticuloModeloLoteFijo extends ArticuloModeloInventario{
    private float puntoPedido;
}
