package com.boxitall.boxitall.repositories;

import com.boxitall.boxitall.entities.EstadoOrdenCompra;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoOrdenCompraRepository extends BaseEntityRepository<EstadoOrdenCompra, Long>{
    Optional<EstadoOrdenCompra> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
