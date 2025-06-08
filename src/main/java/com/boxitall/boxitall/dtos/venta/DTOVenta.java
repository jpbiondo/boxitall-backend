package com.boxitall.boxitall.dtos.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOVenta {
    private Long id;
    private Date fechaVenta;
    private List<DTOVentaDetalle> detalles = new ArrayList<>();
}
