package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticuloRepository extends BaseEntityRepository<Articulo, Long> {
    List<Articulo> findByProvPred(Proveedor proveedor);

    @Query("SELECT art FROM Articulo art " +
            "WHERE art.fechaBaja is not null ")
    List<Articulo> findByBajado();
    List<Articulo> findByFechaBajaIsNullAndProvPredIsNotNull();

    @Query("SELECT DISTINCT a FROM Articulo a " +
            "JOIN a.artProveedores ap " +
            "WHERE ap.proveedor = :proveedor AND a.fechaBaja IS NULL")
    List<Articulo> findArticulosActivosbyProveedor(@Param("proveedor") Proveedor proveedor);

}
