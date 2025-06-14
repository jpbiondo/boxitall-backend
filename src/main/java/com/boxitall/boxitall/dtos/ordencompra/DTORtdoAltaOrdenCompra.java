package com.boxitall.boxitall.dtos.ordencompra;

import com.boxitall.boxitall.entities.OrdenCompra;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTORtdoAltaOrdenCompra {
    private OrdenCompra orden;
    private List<String> errores;
}
