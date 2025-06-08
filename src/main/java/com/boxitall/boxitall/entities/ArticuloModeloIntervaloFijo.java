package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ArticuloModeloIntervaloFijo extends ArticuloModeloInventario {
    private LocalDateTime fechaProximoPedido;
    private int intervaloPedido;
    private float inventarioMaximo;


    //Hecho a mano por problemas con los constructors de lombok
    public ArticuloModeloIntervaloFijo(LocalDateTime fechaProximoPedido, int intervaloPedido, float inventarioMaximo){
        super(0.0f); // TODO sacar el stock de seguridad de acá, debería ser calculado en la clase base
        this.fechaProximoPedido = fechaProximoPedido;
        this.intervaloPedido = intervaloPedido;
        this.inventarioMaximo = inventarioMaximo;
    }
}
