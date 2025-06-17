package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraAlta {
    private List<DTOOrdenCompraArticuloAlta> detallesarticulo;
    private Long IDProveedor;

}