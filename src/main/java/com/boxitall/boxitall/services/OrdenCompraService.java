package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompra;
import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticulo;
import com.boxitall.boxitall.dtos.ordencompra.DTORtdoAltaOrdenCompra;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrdenCompraService extends BaseEntityServiceImpl<OrdenCompra, Long> {
    @Autowired
    EstadoOrdenCompraRepository estadoOrdenCompraRepository;
    @Autowired
    OrdenCompraRepository ordenCompraRepository;
    @Autowired
    OrdenCompraEstadoOCRepository ordenCompraEstadoOCRepository;
    @Autowired
    OrdenCompraArticuloService ordenCompraArticuloService ;
    @Autowired
    ProveedorRepository proveedorRepository;
    @Autowired
    OrdenCompraArticuloRepository ordenCompraArticuloRepository;
    public DTORtdoAltaOrdenCompra altaOrdenCompra(DTOOrdenCompra ordencompradto) {
        List<String> errores = new ArrayList<>();
        OrdenCompra orden = new OrdenCompra();

        try {
            EstadoOrdenCompra estadopendiente = estadoOrdenCompraRepository
                    .findByNombre("PENDIENTE")
                    .orElseThrow(() -> new RuntimeException("Estado 'PENDIENTE' no encontrado."));

            Proveedor proveedor = proveedorRepository.findById(ordencompradto.getIDProveedor())
                    .orElseThrow(() -> new RuntimeException("Proveedor con ID " + ordencompradto.getIDProveedor() + " no encontrado."));

            OrdenCompraEstadoOC estadoactual = new OrdenCompraEstadoOC();
            estadoactual.setEstado(estadopendiente);
            estadoactual.setFechaInicio(new Date());
            ordenCompraEstadoOCRepository.save(estadoactual);

            orden.setFechaInicio(LocalDateTime.now());
            orden.setProveedor(proveedor);
            orden.getHistorialEstados().add(estadoactual);

            for (DTOOrdenCompraArticulo detalleDto : ordencompradto.getDetallesarticulo()) {
                try {
                    OrdenCompraArticulo detalecreado = ordenCompraArticuloService.altaDetalle(detalleDto);
                    orden.getDetalles().add(detalecreado);
                } catch (Exception e) {
                    errores.add("Artículo ID " + detalleDto.getIDarticulo() + ": " + e.getMessage());
                }
            }

            //  si no pued crear ningún detalle
            if (orden.getDetalles().isEmpty()) {
                errores.add("No se pudo crear ningún detalle. Orden no creada.");
                return new DTORtdoAltaOrdenCompra(null,errores);
            }

            OrdenCompra ordenGuardada = ordenCompraRepository.save(orden);
            return new DTORtdoAltaOrdenCompra(ordenGuardada, errores);

        } catch (Exception e) {
            errores.add("Error: " + e.getMessage());
            return new DTORtdoAltaOrdenCompra(null, errores);
        }
    }
    @Transactional
    public void cancelarOrdenCompra(Long ordenCompraId) {
        try {

            OrdenCompra orden = ordenCompraRepository.findById(ordenCompraId)
                    .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));

            OrdenCompraEstadoOC estadoActual = orden.getNombreEstadoActual(orden);
            // verficar si se puede cancelar
            String estadoActualNombre = estadoActual.getEstado().getNombre();
            if (!"PENDIENTE".equals(estadoActualNombre)) {
                throw new RuntimeException("Solo se pueden cancelar órdenes en estado PENDIENTE.");
            }
            // se cierra el estado actual
            estadoActual.setFechaFin(new Date());
            ordenCompraEstadoOCRepository.save(estadoActual);

            EstadoOrdenCompra estadoCancelada = estadoOrdenCompraRepository.findByNombre("CANCELADA")
                    .orElseThrow(() -> new RuntimeException("No se encontró el estado CANCELADA."));
            // crear nuevo estado
            OrdenCompraEstadoOC nuevoEstado = new OrdenCompraEstadoOC();
            nuevoEstado.setEstado(estadoCancelada);
            nuevoEstado.setFechaInicio(new Date());
            nuevoEstado.setFechaFin(null);

            orden.getHistorialEstados().add(nuevoEstado);

            ordenCompraEstadoOCRepository.save(nuevoEstado);

            ordenCompraRepository.save(orden);
        } catch (Exception e) {
            throw new RuntimeException("Error al cancelar la orden de compra: " + e.getMessage(), e);
        }
    }
        public void eliminarDetalleDeOrden(Long idOrden, Long idDetalle) {
            try {
                OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                        .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));
                OrdenCompraEstadoOC estadoActual = orden.getNombreEstadoActual(orden);
                OrdenCompraArticulo detalle = ordenCompraArticuloRepository.findById(idDetalle)
                        .orElseThrow(() -> new RuntimeException("Detalle no encontrado."));
                // verfica si se puede eliminar
                String estadoActualNombre = estadoActual.getEstado().getNombre();
                if (!"PENDIENTE".equals(estadoActualNombre)) {
                    throw new RuntimeException("Solo se pueden modificar órdenes en estado PENDIENTE.");
                }
                orden.getDetalles().remove(detalle);
                ordenCompraRepository.save(orden);
            } catch (Exception e) {
                throw new RuntimeException("Error eliminar el detalle de la orden de compra: " + e.getMessage(), e);
            }
        }
    @Transactional
    public void actualizarCantidadDetalle(Long idOrden, Long idDetalle, Integer nuevaCantidad) {
           try{
            if (nuevaCantidad == null || nuevaCantidad <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a cero.");
            }

            OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                    .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));

            OrdenCompraArticulo detalle = ordenCompraArticuloRepository.findById(idDetalle)
                    .orElseThrow(() -> new RuntimeException("Detalle no encontrado."));

            detalle.setCantidad(nuevaCantidad);
            ordenCompraArticuloRepository.save(detalle);
           } catch (Exception e) {
               throw new RuntimeException("Error al modificar el detalle de la orden de compra: " + e.getMessage(), e);
           }

        }



}
