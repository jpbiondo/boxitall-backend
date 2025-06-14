package com.boxitall.boxitall.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArticuloProveedor extends BaseEntity{
    private float costoCompra;
    private float cargoPedido;
    private float costoPedido;
    private int demoraEntrega; //en días
    private float precioUnitario;
    private float puntoPedido;

    private LocalDateTime fechaBaja;

    //Relaciones
    @ManyToOne
    private Proveedor proveedor;
}
