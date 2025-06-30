package com.boxitall.boxitall.dtos.articulo;

import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class DTOArticuloProveedor {
    private float costoPedido;
    private int demoraEntrega;
    private float precioUnitario;
    private DTOProveedor proveedor;
}
