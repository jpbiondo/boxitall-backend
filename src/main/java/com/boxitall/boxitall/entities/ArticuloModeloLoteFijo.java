package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ArticuloModeloLoteFijo extends ArticuloModeloInventario{
    private float loteOptimo;
    private float puntoPedido;

    //Hecho a mano por problemas con los constructors de lombok
    public ArticuloModeloLoteFijo(float stockSeguridad, float loteOptimo, float puntoPedido) {
        super(stockSeguridad, "LoteFijo");
        this.loteOptimo = loteOptimo;
        this.puntoPedido = puntoPedido;
    }
}
