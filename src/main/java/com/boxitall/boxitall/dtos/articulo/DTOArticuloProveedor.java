package com.boxitall.boxitall.dtos.articulo;

import com.boxitall.boxitall.entities.Proveedor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DTOArticuloProveedor
{
    private float costoCompra;
    private float cargoPedido;
    private float costoPedido;
    private int demoraEntrega; //en días
    private float precioUnitario;
    private float puntoPedido;

    private Proveedor proveedor;
}
