package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Proveedor;

public interface ProveedorRepository extends BaseEntityRepository<Proveedor, Long>{
    boolean existsByProveedorCod(int proveedorCod);
}
