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
    private float loteOptimo;
    private float puntoPedido;

    //Hecho a mano por problemas con los constructors de lombok
    public ArticuloModeloLoteFijo(float loteOptimo, float puntoPedido) {
        super(0.0f, "LoteFijo"); // TODO sacar el stock de seguridad de acá, debería ser calculado en la clase base
        this.loteOptimo = loteOptimo;
        this.puntoPedido = puntoPedido;
    }
}
