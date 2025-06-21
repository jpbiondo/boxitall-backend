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
public class DTOArticuloModeloIntervaloFijo extends DTOArticuloModeloInventario {
    private int intervaloPedido;
    private float inventarioMax;
    private LocalDateTime fechaProximoPedido;
}
