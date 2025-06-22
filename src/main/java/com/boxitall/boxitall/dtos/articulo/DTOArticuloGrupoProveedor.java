package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloGrupoProveedor {
    private Long proveedorId;
    private String proveedorNombre;
    private List<DTOArticuloProveedorListado> articulos;

    public DTOArticuloGrupoProveedor(Long proveedorId, String proveedorNombre) {
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.articulos = new ArrayList<>();
    }
}
