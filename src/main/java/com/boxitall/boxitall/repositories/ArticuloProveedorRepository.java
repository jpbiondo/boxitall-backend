package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.ArticuloProveedor;
import com.boxitall.boxitall.entities.Proveedor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticuloProveedorRepository extends BaseEntityRepository<ArticuloProveedor, Long> {

}
