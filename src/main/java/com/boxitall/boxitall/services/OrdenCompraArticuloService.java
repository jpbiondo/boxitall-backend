package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticulo;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompraArticulo;
import com.boxitall.boxitall.entities.OrdenCompraEstadoOC;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraEstadoOCRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdenCompraArticuloService extends BaseEntityServiceImpl<OrdenCompraArticulo, Long>{
    @Autowired
    OrdenCompraArticuloRepository ordenCompraArticuloRepository;
    @Autowired
    OrdenCompraEstadoOCRepository ordenCompraEstadoOCRepository;
    @Autowired
    ArticuloRepository articuloRepository ;
    @Autowired
    OrdenCompraRepository ordenCompraRepository;
    public OrdenCompraArticulo altaDetalle(DTOOrdenCompraArticulo detalledto) {

        Articulo articulo = articuloRepository.findById(detalledto.getIDarticulo())
          .orElseThrow(() -> new RuntimeException("Artículo con ID " + detalledto.getIDarticulo() + " no encontrado."));

         // verificar si ya hay OC activa
        List<OrdenCompra> ordenesActivas = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
        if (!ordenesActivas.isEmpty()) {
            try {
                throw new Exception("El articulo"+articulo.getId()+"tiene órdenes de compra activas.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

         // a ordenCompraArticulo
        OrdenCompraArticulo detalleArticulo = new OrdenCompraArticulo();
        detalleArticulo.setArticulo(articulo);
        detalleArticulo.setCantidad(detalledto.getCantidad());
         return ordenCompraArticuloRepository.save(detalleArticulo);
    }

}
