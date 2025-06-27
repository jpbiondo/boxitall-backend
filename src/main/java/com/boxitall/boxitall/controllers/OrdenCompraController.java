package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.ordencompra.*;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.services.OrdenCompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/orden-compra")
public class OrdenCompraController extends BaseEntityControllerImpl<OrdenCompra, OrdenCompraService>{
    @PostMapping("/alta-orden-compra")
    public ResponseEntity<?> crearOrdenCompra(@RequestBody DTOOrdenCompraAlta ordenCompraDTO) {
        DTORtdoAltaOrdenCompra resultado =  service.altaOrdenCompra(ordenCompraDTO);
        // Si la orden no se creó
        if (resultado.getOrdenCompraRespuesta() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "mensaje", "No se pudo crear la orden de compra.",
                            "errores", resultado.getErrores()
                    ));
        }
        // Si la orden se creó pero con algunos errores en detalles
        if (!resultado.getErrores().isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of(
                            "mensaje", "Orden de compra creada parcialmente con advertencias.",
                            "orden", resultado.getOrdenCompraRespuesta(),
                            "errores", resultado.getErrores()
                    ));
        }
        // Si la orden se creó sin errores
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje", "Orden de compra creada exitosamente.",
                        "orden", resultado.getOrdenCompraRespuesta()
                ));
    }

    @PutMapping("/{idOrden}/detalle/cancelar-orden")
    public ResponseEntity<?> cancelarOrden(@PathVariable Long idOrden) {
        try {
            service.cancelarOrdenCompra(idOrden);
            return ResponseEntity.ok("Orden de compra cancelada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{idOrden}/detalle/{idDetalle}/eliminar-detalle")
    public ResponseEntity<?> eliminarDetalleDeOrden(@PathVariable Long idOrden, @PathVariable Long idDetalle) {
        try {
            service.eliminarDetalleDeOrden(idOrden, idDetalle);
            return ResponseEntity.ok("Detalle eliminado correctamente de la orden.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PutMapping("/{idOrden}/detalle/{idDetalle}/actualizar-cantidad")
    public ResponseEntity<?> actualizarCantidadDetalle(@PathVariable Long idOrden,
                                                       @PathVariable Long idDetalle,
                                                       @RequestParam("nuevaCantidad") Integer nuevaCantidad) {
        try {
            service.actualizarCantidadDetalle(idOrden, idDetalle, nuevaCantidad);
            return ResponseEntity.ok("Cantidad actualizada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{idOrden}/detalle/avanzar-estado")
    public ResponseEntity<List<String>> avanzarEstado(@PathVariable Long idOrden) {
        List<String> avisos = service.avanzarEstadoOrdenCompra(idOrden);
        return ResponseEntity.ok(avisos);
    }
    @GetMapping("/{idOrden}/detalle")
    public ResponseEntity<DTOOrdenCompraObtenerDetalle> verDetalleOrdenCompra(@PathVariable Long idOrden) {
        DTOOrdenCompraObtenerDetalle respuesta = service.obtenerDetalleOrdenCompra(idOrden);
        return ResponseEntity.ok(respuesta);
    }
    @GetMapping("/activas")
    public ResponseEntity<List<DTOOrdenCompraListadoActivas>> listarOrdenesActivas() {
        List<DTOOrdenCompraListadoActivas> ordenes = service.obtenerOrdenesActivas();

        if (ordenes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ordenes);
    }
    @PostMapping("/{idOrden}/detalle/agregar-articulo")
    public ResponseEntity<DTOOrdenCompraObtenerDetalle> agregarArticuloAOrden(
            @PathVariable Long idOrden,
            @RequestBody DTOOrdenCompraArticuloAlta nuevoDetalleDTO) {

        try {
            DTOOrdenCompraObtenerDetalle resultado = service.agregarArticuloAOrden(idOrden, nuevoDetalleDTO);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}

