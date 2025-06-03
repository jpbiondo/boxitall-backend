package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class ArticuloModeloIntervaloFijo extends ArticuloModeloInventario {
    private LocalDate fechaProximoPedido;
    private int intervaloPedido;
    private float inventarioMaximo;


    //Hecho a mano por problemas con los constructors de lombok
    public ArticuloModeloIntervaloFijo(float stockSeguridad, LocalDate fechaProximoPedido, int intervaloPedido, float inventarioMaximo){
        super(stockSeguridad, "IntervaloFijo");
        this.fechaProximoPedido = fechaProximoPedido;
        this.intervaloPedido = intervaloPedido;
        this.inventarioMaximo = inventarioMaximo;
    }
}
