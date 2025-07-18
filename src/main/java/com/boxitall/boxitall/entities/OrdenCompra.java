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
public class OrdenCompra extends BaseEntity {
    private LocalDateTime fechaInicio;

    public OrdenCompraEstadoOC getEstadoActual(OrdenCompra orden) {
        for (OrdenCompraEstadoOC estadoOC : orden.getHistorialEstados()) {
            if (estadoOC.getFechaFin() == null) {
                return estadoOC;
            }
        }
        return null;
    }
    public String getNombreEstadoActual(OrdenCompra orden) {
        for (OrdenCompraEstadoOC estadoOC : orden.getHistorialEstados()) {
            if (estadoOC.getFechaFin() == null) {
              return estadoOC.getEstado().getNombre();
            }
        }
        return null;
    }





    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_compra_id")
    private List<OrdenCompraEstadoOC> historialEstados = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_compra_id")
    private List<OrdenCompraArticulo> detalles = new ArrayList<>();


}