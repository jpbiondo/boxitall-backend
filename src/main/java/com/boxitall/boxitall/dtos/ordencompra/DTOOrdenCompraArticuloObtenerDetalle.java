package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraArticuloObtenerDetalle {
    private Long IDarticulo;
    private int renglon;
    private String nombreArticulo;
    private float cantidad;
    private float precio;
    private Long idOCarticulo;
    private float loteoptimo;
}