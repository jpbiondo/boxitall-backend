package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DTOArticuloAlta {
    //Artículo
    private String nombre;
    private String descripcion;
    private float costoAlmacenamiento;
    private float demanda;
    private float desviacionEstandar;
    private float nivelServicio;
    private float stock;

    //Proveedor predeterminado
    private Long provPredId;

    //Proveedores
    private List<DTOArticuloAddProveedor> articuloProveedores;

    //Modelo inventario
    private DTOArticuloModeloInventarioAlta modeloInventario;

    // VIEJO
    //private String modeloNombre;
    //Intervalo fijo
    //private int intervaloPedido;
    //private float inventarioMaximo;

}
