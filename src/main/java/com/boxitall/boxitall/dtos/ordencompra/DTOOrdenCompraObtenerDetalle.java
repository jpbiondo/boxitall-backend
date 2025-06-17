package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraObtenerDetalle {
    private Long IDOrdenCompra;
    private List<DTOOrdenCompraArticuloObtenerDetalle> detalleArticulos;
    private String estado ;

}
