package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompra {
    private List<DTOOrdenCompraArticulo> detallesarticulo;
    private Long IDProveedor;

}