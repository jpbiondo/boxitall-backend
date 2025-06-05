package com.boxitall.boxitall.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Venta extends BaseEntity {
    private Date fechaVenta;

    //Relaciones
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "venta")
    private List<VentaDetalle> detalle = new ArrayList<>();
}
