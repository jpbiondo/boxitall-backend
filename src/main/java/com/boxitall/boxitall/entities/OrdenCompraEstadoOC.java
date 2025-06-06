package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class OrdenCompraEstadoOC extends BaseEntity{
   private Date fechaFin;
   private Date fechaInicio;
    @ManyToOne
    @JoinColumn
    private EstadoOrdenCompra estado;
    @ManyToOne
    @JoinColumn
    private OrdenCompra ordenCompra;
}
