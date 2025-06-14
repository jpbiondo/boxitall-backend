package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrdenCompra extends BaseEntity {
    private LocalDateTime fechaInicio;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    //private String estado; //revisar, lo puse para hacer la logica de que no puedo dar de baja un proveedor si hay una oc pendiente
}