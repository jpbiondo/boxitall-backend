package com.boxitall.boxitall.dtos.proveedor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DTOProveedor {
    private Long proveedorId;
    private int proveedorCod;
    private String proveedorNombre;
    private String proveedorTelefono;
    private LocalDateTime proveedorFechaBaja;

}
