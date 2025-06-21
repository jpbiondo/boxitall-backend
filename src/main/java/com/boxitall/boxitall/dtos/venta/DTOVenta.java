package com.boxitall.boxitall.dtos.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOVenta {
    private Long id;
    private LocalDateTime fechaVenta;
    private List<DTOVentaDetalle> detalles = new ArrayList<>();
}
