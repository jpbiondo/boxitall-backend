package com.boxitall.boxitall.dtos.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class DTOVentaAlta {
    private Map<Long, Float> articuloIdCantidad;
}
