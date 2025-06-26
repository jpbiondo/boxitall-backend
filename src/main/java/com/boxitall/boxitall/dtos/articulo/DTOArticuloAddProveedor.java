package com.boxitall.boxitall.dtos.articulo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DTOArticuloAddProveedor {
    private Long articuloId;
    private Long proveedorId;

    private float costoCompra;
    private float cargoPedido;
    private float costoPedido;
    private int demoraEntrega; //en días
    private float precioUnitario;
}
