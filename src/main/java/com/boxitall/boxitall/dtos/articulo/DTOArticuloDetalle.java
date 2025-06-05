package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloDetalle {
    //Artículo
    private Long id;
    private String nombre;
    private float stock;
    private String descripcion;
    private float costoAlmacenamiento;

    //Modelo inventario
    private String modeloInventario;
    private Date fechaProximoPedido; // Para modelo intervalo fijo
    private float stockProximoPedido; // Para modelo lote fijo

    //Proveedor predeterminado
    private Long proveedorPredeterminadoId;
    private String proveedorPredeterminadoNombre;

    //CGI
    private float cgiCostoPedido;
    private float cgiCostoAlmacenamiento;
    private float cgiCostoCompra;
    private float cgiTotal;
    private float cgiUnidades;

    // Listado proveedores?


}
