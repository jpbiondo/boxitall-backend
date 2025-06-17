package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompraArticulo;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenCompraRepository extends BaseEntityRepository<OrdenCompra, Long>{
    @Query("SELECT o FROM OrdenCompra o " +
            "JOIN o.historialEstados estadoOC " +
            "JOIN estadoOC.estado estado " +
            "WHERE o.proveedor = :proveedor " +
            "AND estadoOC.fechaFin IS NULL " +
            "AND estado.nombre IN ('PENDIENTE', 'ENVIADA')")
    List<OrdenCompra> findOrdenesActivasbyProveedor(@Param("proveedor") Proveedor proveedor);

    @Query("""
    SELECT o FROM OrdenCompra o
    JOIN o.detalles oa
    JOIN o.historialEstados estadoOC
    JOIN estadoOC.estado e
    WHERE oa.articulo = :articulo
      AND estadoOC.fechaFin IS NULL
      AND e.nombre IN ('PENDIENTE', 'ENVIADA')
""")
    List<OrdenCompra> findOrdenesActivasByArticulo(@Param("articulo") Articulo articulo);
    @Query("SELECT o FROM OrdenCompra o " +
            "JOIN o.historialEstados he " +
            "WHERE he.fechaFin IS NULL AND he.estado.nombre IN ('PENDIENTE', 'ENVIADA')")
    List<OrdenCompra> findOrdenesActivas();




}
