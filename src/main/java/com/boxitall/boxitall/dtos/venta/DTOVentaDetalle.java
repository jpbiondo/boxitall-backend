package com.boxitall.boxitall.dtos.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DTOVentaDetalle {
    private Long id;
    private float cantidad;
    private int renglon;
    private Long idArt;
    private String nombreArt;
}
