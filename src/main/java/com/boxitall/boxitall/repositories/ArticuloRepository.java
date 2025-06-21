package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticuloRepository extends BaseEntityRepository<Articulo, Long>{
    List<Articulo> findByProvPred(Proveedor proveedor);

    @Query("SELECT art FROM Articulo art " +
            "WHERE art.fechaBaja is not null ")
    List<Articulo> findByBajado();
}
