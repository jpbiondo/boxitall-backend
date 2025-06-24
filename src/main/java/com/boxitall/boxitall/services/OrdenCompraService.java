package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.ordencompra.*;
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
    @Autowired
    ArticuloRepository articuloRepository;
   @Transactional
    public DTORtdoAltaOrdenCompra altaOrdenCompra(DTOOrdenCompraAlta ordencompradto) {
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
             int renglon = 1 ;
            for (DTOOrdenCompraArticuloAlta detalleDto : ordencompradto.getDetallesarticulo()) {
                try {
                    OrdenCompraArticulo detalecreado = ordenCompraArticuloService.altaDetalle(detalleDto);
                    detalecreado.setRenglon(renglon);
                    orden.getDetalles().add(detalecreado);
                    renglon++ ;
                } catch (Exception e) {
                    errores.add("Artículo ID " + detalleDto.getIDarticulo() + ": " + e.getMessage());
                }
            }

            //  si no pued crear ningún detalle
            List<OrdenCompraArticulo> detalles = orden.getDetalles();
            if (detalles.isEmpty()) {
                errores.add("No se pudo crear ningún detalle. Orden no creada.");
                return new DTORtdoAltaOrdenCompra(null,errores);
            }
            ordenCompraRepository.save(orden);
            DTOOrdenCompraObtenerDetalle ordenGuardadadto = obtenerDetalleOrdenCompra(orden.getId());

            return new DTORtdoAltaOrdenCompra(ordenGuardadadto, errores);

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

            OrdenCompraEstadoOC estadoActual = orden.getEstadoActual(orden);
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
    @Transactional
    public void eliminarDetalleDeOrden(Long idOrden, Long idDetalle) {
        try {
            OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                    .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));
            OrdenCompraEstadoOC estadoActual = orden.getEstadoActual(orden);
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
    @Transactional
    public List<String> avanzarEstadoOrdenCompra(Long ordenCompraId) {
        List<String> avisos = new ArrayList<>();
        try {
            OrdenCompra orden = ordenCompraRepository.findById(ordenCompraId)
                    .orElseThrow(() -> new RuntimeException("Órden de compra no encontrada."));

            OrdenCompraEstadoOC estadoActual = orden.getEstadoActual(orden);
            String estadoActualNombre = estadoActual.getEstado().getNombre();

            // Determinar siguiente estado
            String proximoEstadoNombre = getProximoEstado(estadoActualNombre);
            EstadoOrdenCompra proximoEstado = estadoOrdenCompraRepository.findByNombre(proximoEstadoNombre)
                    .orElseThrow(() -> new RuntimeException("No se encontró el estado " + proximoEstadoNombre + "."));
            OrdenCompraEstadoOC nuevoEstado = new OrdenCompraEstadoOC();
            nuevoEstado.setEstado(proximoEstado);
            nuevoEstado.setFechaInicio(new Date());
            nuevoEstado.setFechaFin(null);

            orden.getHistorialEstados().add(nuevoEstado);

            // Si el nuevo estado es finaliza  reponer stock y controlar Punto de Pedido
            if ("FINALIZADA".equals(proximoEstadoNombre)) {
                avisos.addAll(reponerStockYControlarPuntoPedido(orden));
            }
            // Cerrar estado actual
            estadoActual.setFechaFin(new Date());
            ordenCompraEstadoOCRepository.save(estadoActual);
            ordenCompraRepository.save(orden);
            return avisos;

        } catch (Exception e) {
            throw new RuntimeException("Error al avanzar el estado de la órden de compra: " + e.getMessage(), e);
        }
    }
    @Transactional
    public DTOOrdenCompraObtenerDetalle obtenerDetalleOrdenCompra(Long idOrden) {
        try{
            OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                    .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));
            List<DTOOrdenCompraArticuloObtenerDetalle> detalleArticulos = new ArrayList<>();

            for (OrdenCompraArticulo detalle : orden.getDetalles()) {
                Articulo articulo = detalle.getArticulo();
                float precio = 0f;
                boolean encontrado = false;

                for (ArticuloProveedor artproveedor : articulo.getArtProveedores()) {
                    if (artproveedor.getProveedor().getId().equals(orden.getProveedor().getId())) {
                        precio = artproveedor.getPrecioUnitario();
                    }
                }
                DTOOrdenCompraArticuloObtenerDetalle dtoDetalle = new DTOOrdenCompraArticuloObtenerDetalle(
                        detalle.getArticulo().getId(),
                        detalle.getRenglon(),
                        detalle.getArticulo().getNombre(),
                        detalle.getCantidad(),
                        precio,
                        detalle.getId(),
                        detalle.getArticulo().getModeloInventario().getLoteOptimo()
                );
                detalleArticulos.add(dtoDetalle);
            }
            return new DTOOrdenCompraObtenerDetalle(orden.getId(),detalleArticulos,orden.getNombreEstadoActual(orden),orden.getProveedor().getProveedorNombre(),orden.getProveedor().getId());
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el detalle de la órden de compra: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<DTOOrdenCompraListadoActivas> obtenerOrdenesActivas(){
        try{
            List<OrdenCompra> ordenes = ordenCompraRepository.findOrdenesActivas();
            if (ordenes.isEmpty()){
                return null;
            }
            List<DTOOrdenCompraListadoActivas> ordenesactivas =  new ArrayList<>();
            for(OrdenCompra orden : ordenes){
                DTOOrdenCompraListadoActivas ordenactiva = new DTOOrdenCompraListadoActivas(
                        orden.getId(),
                        orden.getFechaInicio(),
                        orden.getNombreEstadoActual(orden),
                        orden.getProveedor().getProveedorNombre());
                ordenesactivas.add(ordenactiva);
            }
            return ordenesactivas;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener las órdenes de compra: " + e.getMessage(), e);
        }
    }
    @Transactional
    public DTOOrdenCompraObtenerDetalle agregarArticuloAOrden(Long idOrden, DTOOrdenCompraArticuloAlta nuevodetalledto) {
        try {
            OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                    .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada."));

            OrdenCompraEstadoOC estadoActual = orden.getEstadoActual(orden);
            if (!"PENDIENTE".equals(estadoActual.getEstado().getNombre())) {
                throw new RuntimeException("Solo se pueden modificar órdenes en estado PENDIENTE.");
            }
            OrdenCompraArticulo nuevoDetalle = ordenCompraArticuloService.altaDetalle( nuevodetalledto);
            orden.getDetalles().add(nuevoDetalle);
            ordenCompraRepository.save(orden);

            return obtenerDetalleOrdenCompra(idOrden);
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar artículo a la orden: " + e.getMessage(), e);
        }
    }






    // Metodos Auxiliares
    private String getProximoEstado(String estadoActualNombre) {
        switch (estadoActualNombre) {
            case "PENDIENTE": return "ENVIADA";
            case "ENVIADA": return "FINALIZADA";
            default: throw new RuntimeException("Estado desconocido");
        }
    }
    private List<String> reponerStockYControlarPuntoPedido(OrdenCompra orden) {
        List<String> avisos = new ArrayList<>();

        for (OrdenCompraArticulo detalle : orden.getDetalles()) {
            Articulo articulo = detalle.getArticulo();
            double cantidad = detalle.getCantidad();
            float nuevaCantidad = (float) (articulo.getStock() + cantidad);

            articulo.setStock(nuevaCantidad);
            articuloRepository.save(articulo);

            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo modelo) {
                float puntoPedido = modelo.getPuntoPedido();
                if (nuevaCantidad <= puntoPedido) {
                    avisos.add("El artículo '" + articulo.getNombre() + "' tiene stock " + nuevaCantidad +
                            ", que no supera el Punto de Pedido (" + puntoPedido + ")");
                }
            }
        }

        return avisos;
    }








}