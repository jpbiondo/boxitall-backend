package com.boxitall.boxitall.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DTOArticuloAlta {
    //Artículo
    private String nombre;
    private String descripcion;
    private float costoAlmacenamiento;
    private float demanda;
    private float demandaDesviacionEstandar;
    private float nivelServicio;
    private float stock;

    //Modelo inventario
    private String modeloNombre;
    //Intervalo fijo
    private int intervaloPedido;
    private float inventarioMaximo;
    //Lote fijo
    private float loteOptimo;
    private float puntoPedido;
}
