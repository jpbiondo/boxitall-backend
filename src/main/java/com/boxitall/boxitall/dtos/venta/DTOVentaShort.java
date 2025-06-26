package com.boxitall.boxitall.dtos.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DTOVentaShort {
    private Long id;
    private LocalDateTime fechaVenta;
}
