package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompra;
import com.boxitall.boxitall.dtos.ordencompra.DTORtdoAltaOrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.services.OrdenCompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/orden-compra")
public class OrdenCompraController extends BaseEntityControllerImpl<OrdenCompra, OrdenCompraService>{
    @PostMapping("/altaordencompra")
    public ResponseEntity<?> crearOrdenCompra(@RequestBody DTOOrdenCompra ordenCompraDTO) {
        DTORtdoAltaOrdenCompra resultado =  service.altaOrdenCompra(ordenCompraDTO);
        // Si la orden no se creó
        if (resultado.getOrden() == null) {
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
                            "orden", resultado.getOrden(),
                            "errores", resultado.getErrores()
                    ));
        }
        // Si la orden se creó sin errores
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje", "Orden de compra creada exitosamente.",
                        "orden", resultado.getOrden()
                ));
    }

   @PutMapping("/cancelar/{id}")
    public ResponseEntity<?> cancelarOrden(@PathVariable Long id) {
        try {
            service.cancelarOrdenCompra(id);
            return ResponseEntity.ok("Orden de compra cancelada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{idOrden}/detalle/{idDetalle}")
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


}

