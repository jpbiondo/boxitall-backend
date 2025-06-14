package com.boxitall.boxitall.entities;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EstadoOrdenCompra extends BaseEntity{
    private LocalDateTime fechaBaja;
    private String nombre;  // PENDIENTE, ENVIADA, CANCELADA, FINALIZADA
}
