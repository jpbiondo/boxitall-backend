package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor

public class DTOArticuloProveedorListado {
    private Long idArticulo;
    private String nombreArticulo;
    private float precioProveedor;
    private boolean esProveedorPredeterminado;
}
