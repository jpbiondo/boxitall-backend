package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenCompraRepository extends BaseEntityRepository<OrdenCompra, Long>{
    boolean existsByProveedorAndEstadoIn(Proveedor proveedor, List<String> estados);
}
