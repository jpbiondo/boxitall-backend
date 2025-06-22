package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOArticuloModeloInventarioAlta {
    private String nombre;

    // Para intervalo fijo
    private LocalDateTime fechaProxPedido;
    private float inventarioMaximo;
    private int intervaloPedido;
}
