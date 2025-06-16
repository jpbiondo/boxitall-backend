package com.boxitall.boxitall.dtos.proveedor;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DTOAltaProveedor {
    private DTOProveedor dtoProveedor;
    private DTOArticuloAddProveedor dtoArticuloAddProveedor;
}
