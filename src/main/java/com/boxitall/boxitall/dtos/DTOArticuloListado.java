package com.boxitall.boxitall.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    private Date fechaProximoPedido;
    private float stockProximoPedido;
    private Long proveedorPredeterminadoId;
    private String proveedorPredeterminadoNombre;
}
