package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.DTOOrdenCompraArticulo;
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
    public void altaDetalle(DTOOrdenCompraArticulo detalledto, OrdenCompra orden) {

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
        detalleArticulo.setOrdenCompra(orden);
        detalleArticulo.setArticulo(articulo);
        detalleArticulo.setCantidad(detalledto.getCantidad());
        ordenCompraArticuloRepository.save(detalleArticulo);
    }
    @Transactional
    public void delate (Long id){
        // Buscar el detalle
        OrdenCompraArticulo detalle = ordenCompraArticuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle con id " + id + " no encontrado."));

        OrdenCompra orden = detalle.getOrdenCompra();
        OrdenCompraEstadoOC estadoActual = ordenCompraEstadoOCRepository
                .findByOrdenCompraAndFechaFinIsNull(detalle.getOrdenCompra())
                .orElseThrow(() -> new RuntimeException("Estado actual no encontrado para la orden."));
        if (!"PENDIENTE".equalsIgnoreCase(estadoActual.getEstado().getNombre())) {
            throw new RuntimeException("No se puede eliminar el detalle porque la orden está en estado distinto de PENDIENTE.");
        }
        ordenCompraArticuloRepository.delete(detalle);
    }

    @Transactional
    public OrdenCompraArticulo updatecantidad(Long id, float nuevaCantidad) {
        // buscar el detalle
        OrdenCompraArticulo detalle = ordenCompraArticuloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle con id " + id + " no encontrado."));
        // verificar estado actual de la orden
        OrdenCompra orden = detalle.getOrdenCompra();
        OrdenCompraEstadoOC estadoActual = ordenCompraEstadoOCRepository
                .findByOrdenCompraAndFechaFinIsNull(detalle.getOrdenCompra())
                .orElseThrow(() -> new RuntimeException("Estado actual no encontrado para la orden."));

        //Validar que el estado sea PENDIENTE
        if (!"PENDIENTE".equalsIgnoreCase(estadoActual.getEstado().getNombre())) {
            throw new RuntimeException("No se puede modificar la orden porque está en estado ENVIADA.");
        }
        //Actualizar la cantidad
        detalle.setCantidad(nuevaCantidad);

        return ordenCompraArticuloRepository.save(detalle);
    }

}
