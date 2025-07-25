package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloFaltante{
    private Long id;
    private String nombre;
    private float stock;

    private float stockSeguridad;

    private String modeloInventario;
    private LocalDateTime fechaProximoPedido;
    private float restanteProximoPedido;
    private Long proveedorPredeterminadoId;
    private String proveedorPredeterminadoNombre;
}
