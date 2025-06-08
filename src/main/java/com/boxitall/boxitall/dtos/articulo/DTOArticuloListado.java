package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloListado {
    private Long id;
    private String nombre;
    private float stock;
    private String modeloInventario;
    private LocalDateTime fechaProximoPedido;
    private float stockProximoPedido;
    private Long proveedorPredeterminadoId;
    private String proveedorPredeterminadoNombre;
}
