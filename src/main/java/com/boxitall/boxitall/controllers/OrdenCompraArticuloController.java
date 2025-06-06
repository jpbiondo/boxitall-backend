package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.OrdenCompraArticulo;
import com.boxitall.boxitall.services.OrdenCompraArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/orden-compra-articulo")
public class OrdenCompraArticuloController  extends BaseEntityControllerImpl<OrdenCompraArticulo, OrdenCompraArticuloService> {
    @Autowired
    private OrdenCompraArticuloService ordenCompraArticuloService;
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            ordenCompraArticuloService.delate(id);
            return ResponseEntity.ok().body("Detalle eliminado correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno, por favor intente más tarde.");
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OrdenCompraArticulo OCarticulo) {
        try {
            OrdenCompraArticulo actualizado = ordenCompraArticuloService.updatecantidad(id, OCarticulo.getCantidad());
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno, por favor intente más tarde.");
        }
    }
}
