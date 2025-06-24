package com.boxitall.boxitall.dtos.ordencompra;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraAlta {
    private List<DTOOrdenCompraArticuloAlta> detallesarticulo;
    @JsonProperty("IDProveedor")
    private Long IDProveedor;

}