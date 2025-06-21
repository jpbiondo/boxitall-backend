package com.boxitall.boxitall.services;


import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticuloAlta;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraEstadoOCRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public OrdenCompraArticulo altaDetalle(DTOOrdenCompraArticuloAlta detalledto) {

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



        // Obtener el modelo de inventario
        String modeloNombre = articulo.getModeloInventario().getClass().toString();
        int length = modeloNombre.length() - 1;
        int index = 0;
        for (int i = length; i > 0; i--) {
            if (modeloNombre.charAt(i) == '.') {
                index = i + 1 + 14;  // El +1 es para que no empiece desde el punto, el + 14 para que no incluya "ArticuloModelo"
                break;
            }
        }
        modeloNombre = modeloNombre.substring(index);
        // Si es de intervalo fijo le settea la fecha de próximo pedido, si es otro no hace nada
        if (modeloNombre.equals("IntervaloFijo")) {
            ArticuloModeloIntervaloFijo modeloInventario = (ArticuloModeloIntervaloFijo) articulo.getModeloInventario();
            // Trunca la fecha a días (para que sea al inicio del mismo) y le suma el intervalo de pedido
            modeloInventario.setFechaProximoPedido(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(modeloInventario.getIntervaloPedido()));
        }


        return ordenCompraArticuloRepository.save(detalleArticulo);
    }


}
