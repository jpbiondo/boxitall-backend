package com.boxitall.boxitall.dtos.proveedor;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloProveedor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DTOAltaProveedor {
    private DTOProveedor dtoProveedor;
    private DTOArticuloProveedor dtoArticuloProveedor;
}
