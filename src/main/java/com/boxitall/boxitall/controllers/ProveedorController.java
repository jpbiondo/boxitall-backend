package com.boxitall.boxitall.controllers;


import com.boxitall.boxitall.dtos.proveedor.DTOAltaProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.services.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/proveedor")
public class ProveedorController extends BaseEntityControllerImpl<Proveedor, ProveedorService>{
    @Autowired
    private ProveedorService proveedorService;


    @PostMapping("/alta")
    public ResponseEntity<?> altaProveedor(@RequestBody DTOAltaProveedor dtoAltaProveedor) {
        try {

            Proveedor savedProveedor = proveedorService.altaProveedor(dtoAltaProveedor);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedProveedor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            boolean isDeleted = proveedorService.delete(id);  // Llamamos al servicio para dar de baja el proveedor
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Proveedor dado de baja exitosamente\"}");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Proveedor no encontrado\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/listAll")
    public ResponseEntity<?> listAll() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(proveedorService.listAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente más tarde\"}");
        }
    }


}