package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        super(0.0f,0);
        this.fechaProximoPedido = fechaProximoPedido;
        this.intervaloPedido = intervaloPedido;
        this.inventarioMaximo = inventarioMaximo;
    }
}
