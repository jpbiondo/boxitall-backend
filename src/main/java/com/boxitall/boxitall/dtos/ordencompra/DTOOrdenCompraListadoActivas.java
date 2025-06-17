package com.boxitall.boxitall.dtos.ordencompra;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOOrdenCompraListadoActivas {
    private Long IDOrdenCompra;
    private LocalDateTime fecha ;
    private String estado ;
    private String proveedor ;
}
