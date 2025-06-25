package com.boxitall.boxitall.dtos.ordencompra;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraArticuloAlta {
    float cantidad ;
    @JsonProperty("IDarticulo")
    Long IDarticulo;
}
