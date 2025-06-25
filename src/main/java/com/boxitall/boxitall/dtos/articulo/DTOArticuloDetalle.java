package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloDetalle {
    //Artículo
    private Long id;
    private String nombre;
    private float stock;
    private float demanda;
    private String descripcion;
    private float costoAlmacenamiento;
    private float nivelServicio;
    private float desviacionEstandar;

    //Modelo inventario
    private DTOArticuloModeloInventario modeloInventario;
    private float restanteProximoPedido;

    //Proveedor predeterminado
    private Long proveedorPredeterminadoId;
    private String proveedorPredeterminadoNombre;

    //CGI
    private DTOCGI cgi;

    // Todos los demás proveedores
    private List<DTOArticuloProveedor> articuloProveedores;
}

