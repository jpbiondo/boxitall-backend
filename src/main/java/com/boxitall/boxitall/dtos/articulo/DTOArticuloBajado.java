package com.boxitall.boxitall.dtos.articulo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTOArticuloBajado {
    private Long id;
    private String nombre;
    private String descripcion;
    private float costoAlmacenamiento;
    private float nivelServicio;
    private LocalDateTime fechaBaja;
}
