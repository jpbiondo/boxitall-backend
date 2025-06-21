package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DTOArticuloAddProveedor {
    private Long articuloId;
    private Long proveedorId;

    private float costoCompra;
    private float cargoPedido;
    private float costoPedido;
    private int demoraEntrega; //en días
    private float precioUnitario;
}
