package com.boxitall.boxitall.controllers;


import com.boxitall.boxitall.dtos.articulo.DTOArticuloProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOAltaProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.services.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

   /* @GetMapping("/proveedor/{idProveedor}/articulos")
    public ResponseEntity<?> obtenerArticulosPorProveedor(@PathVariable Long idProveedor) {
        try {
            List<Articulo> articulos = proveedorService.obtenerArticulosPorProveedor(idProveedor);

            if (articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("{\"mensaje\":\"No se encontraron artículos para este proveedor\"}");
            }

            // Devolver la lista de artículos que provee ese proveedor
            return ResponseEntity.ok(articulos);

        } catch (Exception e) {
            // En caso de error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }*/
}