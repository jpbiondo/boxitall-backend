package com.boxitall.boxitall.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Venta extends BaseEntity {
    private LocalDateTime fechaVenta;

    //Relaciones
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalle = new ArrayList<>();
}
