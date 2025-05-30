package com.boxitall.boxitall.entities;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Articulo extends BaseEntity {
    private String nombre;
    private String descripcion;
    private float costoAlmacenamiento;
    private float demanda;
    private float demandaDesviacionEstandar;
    private float nivelServicio;
    private float stock;
    private Date fechaBaja;
    private Proveedor provPred;
    private List<ArticuloProveedor> artProveedores;
}
