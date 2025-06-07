package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenCompraRepository extends BaseEntityRepository<OrdenCompra, Long>{
    @Query("SELECT o FROM OrdenCompra o " +
            "JOIN OrdenCompraEstadoOC estado ON estado.ordenCompra = o " +
            "JOIN EstadoOrdenCompra e ON estado.estado = e " +
            "WHERE o.proveedor = :proveedor " +
            "AND estado.fechaFin IS NULL " +
            "AND e.nombre IN ('PENDIENTE', 'ENVIADA')")
    List<OrdenCompra> findOrdenesActivasByProveedor(@Param("proveedor") Proveedor proveedor);
    @Query("SELECT o FROM OrdenCompra o " +
            "JOIN OrdenCompraArticulo oa ON oa.ordenCompra = o " +
            "JOIN OrdenCompraEstadoOC estado ON estado.ordenCompra = o " +
            "JOIN EstadoOrdenCompra e ON estado.estado = e " +
            "WHERE oa.articulo = :articulo " +
            "AND estado.fechaFin IS NULL " +
            "AND e.nombre IN ('PENDIENTE', 'ENVIADA')")
    List<OrdenCompra> findOrdenesActivasByArticulo(@Param("articulo") Articulo articulo);
}
