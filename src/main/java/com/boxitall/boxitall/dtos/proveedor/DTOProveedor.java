package com.boxitall.boxitall.dtos.proveedor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class DTOProveedor {
    private Long id;
    private int proveedorCod;
    private String proveedorNombre;
    private String proveedorTelefono;
    private Date proveedorFechaBaja;

}
