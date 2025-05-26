package com.boxitall.boxitall.entities;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class EstadoOrdenCompra extends BaseEntity{
    private Date fechaBaja;
    private String nombre;
}
