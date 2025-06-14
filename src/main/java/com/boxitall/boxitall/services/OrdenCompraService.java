package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.DTOOrdenCompra;
import com.boxitall.boxitall.dtos.DTOOrdenCompraArticulo;
import com.boxitall.boxitall.entities.EstadoOrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompraEstadoOC;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.EstadoOrdenCompraRepository;
import com.boxitall.boxitall.repositories.OrdenCompraEstadoOCRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
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
    public OrdenCompra altaOrdenCompra(DTOOrdenCompra ordencompradto) {
        //  Buscar el estado pendiente
        EstadoOrdenCompra estadopendiente = estadoOrdenCompraRepository
                .findByNombre("PENDIENTE")
                .orElseThrow(() -> new RuntimeException("Estado 'PENDIENTE' no encontrado."));
        // Buscar proveedor
        Proveedor proveedor = proveedorRepository.findById(ordencompradto.getIDProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor con ID " + ordencompradto.getIDProveedor() + " no encontrado."));

        // Crear la orden de compra
        OrdenCompra orden = new OrdenCompra();
        orden.setFechaInicio(LocalDateTime.now());
        orden.setProveedor(proveedor);
        orden = ordenCompraRepository.save(orden);
        // EstadoActual
        OrdenCompraEstadoOC estadoactual = new OrdenCompraEstadoOC();
        estadoactual.setEstado(estadopendiente);
        estadoactual.setOrdenCompra(orden);
        estadoactual.setFechaInicio(new Date());
        estadoactual.setFechaFin(null);
        ordenCompraEstadoOCRepository.save(estadoactual);
        // Delegar la creacion de los detalles?
        List<String> errores = new ArrayList<>();

        for (DTOOrdenCompraArticulo detalleDto : ordencompradto.getDetallesarticulo()) {
            try {
                ordenCompraArticuloService.altaDetalle(detalleDto, orden);
            } catch (Exception e) {
                errores.add("Artículo ID " + detalleDto.getIDarticulo() + ": " + e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            throw new RuntimeException("Orden creada parcialmente. Errores: " + String.join("; ", errores));
        }
        return orden;
    }
    @Transactional
    public void cancelarOrdenCompra(Long ordenCompraId) {
        OrdenCompra orden = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));

        // Buscar el estado actual
        OrdenCompraEstadoOC estadoActual = ordenCompraEstadoOCRepository
                .findByOrdenCompraAndFechaFinIsNull(orden)
                .orElseThrow(() -> new RuntimeException("No se encontró el estado actual de la orden."));

        String estadoActualNombre = estadoActual.getEstado().getNombre();
        if (!estadoActualNombre.equals("PENDIENTE") && !estadoActualNombre.equals("ENVIADA")) {
            throw new RuntimeException("Solo se pueden cancelar órdenes en estado PENDIENTE o ENVIADA.");
        }

        // Cerrar el estado actual
        estadoActual.setFechaFin(new Date());
        ordenCompraEstadoOCRepository.save(estadoActual);

        EstadoOrdenCompra estadoCancelada = estadoOrdenCompraRepository.findByNombre("CANCELADA")
                .orElseThrow(() -> new RuntimeException("No se encontró el estado CANCELADA."));

        // Crear nuevo estado
        OrdenCompraEstadoOC nuevoEstado = new OrdenCompraEstadoOC();
        nuevoEstado.setOrdenCompra(orden);
        nuevoEstado.setEstado(estadoCancelada);
        nuevoEstado.setFechaInicio(new Date());
        nuevoEstado.setFechaFin(null);

        ordenCompraEstadoOCRepository.save(nuevoEstado);
    }


    }
