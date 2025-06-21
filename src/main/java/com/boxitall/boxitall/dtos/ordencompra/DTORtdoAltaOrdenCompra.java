package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTORtdoAltaOrdenCompra {
    private DTOOrdenCompraObtenerDetalle ordenCompraRespuesta;
    private List<String> errores;
}
