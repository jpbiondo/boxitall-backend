package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOArticuloModeloLoteFijo extends DTOArticuloModeloInventario {
    private float loteOptimo;
    private float puntoPedido;
}
