package com.boxitall.boxitall.dtos.proveedor;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DTOAltaProveedor {
    private int codigo;
    private String nombre;
    private String telefono;
    private List<DTOArticuloAddProveedor> proveedorArticulos;
}
