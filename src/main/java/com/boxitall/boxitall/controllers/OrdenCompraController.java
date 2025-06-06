package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.DTOOrdenCompra;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.services.OrdenCompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/orden-compra")
public class OrdenCompraController extends BaseEntityControllerImpl<OrdenCompra, OrdenCompraService>{
   @PostMapping("/alta")
    public ResponseEntity<?> altaOrdenCompra(@RequestBody DTOOrdenCompra ordenCompraDto) {
        try {
            return ResponseEntity.ok(service. altaOrdenCompra(ordenCompraDto));
        } catch (Exception e)
        { e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear la orden de compra: " + e.getMessage());
        }
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

}

