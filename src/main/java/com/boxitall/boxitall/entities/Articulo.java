package com.boxitall.boxitall.entities;

import com.boxitall.boxitall.exceptions.BadArticle;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Articulo extends BaseEntity {
    private String nombre;
    @Column(length = 1500) // 1500 caracteres de descripción (una guasada)
    private String descripcion;
    private float costoAlmacenamiento;
    private float demanda; //demanda anual
    private float demandaDesviacionEstandar;
    private float nivelServicio;
    private float stock;

    private LocalDateTime fechaBaja;

    //Relaciones
    @ManyToOne
    private Proveedor provPred;

    @OneToMany(cascade = CascadeType.ALL)
    private List<ArticuloProveedor> artProveedores;

    @OneToOne(cascade = CascadeType.ALL)
    private ArticuloModeloInventario modeloInventario;

    public Articulo(String nombre, String descripcion, float costoAlmacenamiento, float demanda, float demandaDesviacionEstandar, float nivelServicio, float stock, ArticuloModeloInventario modeloInventario) throws BadArticle {
        this.nombre = nombre;
        this.descripcion = descripcion;
        if (costoAlmacenamiento < 0) throw new BadArticle("El costo de almacenamiento no puede ser negativo");
        this.costoAlmacenamiento = costoAlmacenamiento;
        if (demanda < 0) throw new BadArticle("La demanda no puede ser negativa");
        this.demanda = demanda;
        if (demandaDesviacionEstandar < 0) throw new BadArticle("La desviación estándar de la demanda no puede ser negativa");
        this.demandaDesviacionEstandar = demandaDesviacionEstandar;
        if (nivelServicio < 0.5 || nivelServicio > 1) throw new BadArticle("El nivel de servicio debe ser un valor entre 0.5 y 1");
        this.nivelServicio = nivelServicio;
        if (stock < 0) throw new BadArticle("El stock no puede ser negativo");
        this.stock = stock;
        this.modeloInventario = modeloInventario;
    }
}
