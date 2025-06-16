package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAlta;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloDetalle;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/articulo")
public class ArticuloController extends BaseEntityControllerImpl<Articulo, ArticuloService> {

    @Autowired
    private ArticuloService servicio;

    @GetMapping("/listAll")
    public ResponseEntity<?> listAll() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(servicio.listAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente más tarde\"}");
        }
    }

    @GetMapping("/getDetalles")
    public ResponseEntity<?> getDetalles(@RequestParam Long id) {
        try {
            DTOArticuloDetalle dtoDetalle = servicio.getArticuloDetalle(id);
            return ResponseEntity.status(HttpStatus.OK).body(dtoDetalle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addArticle(@RequestBody DTOArticuloAlta dtoAlta) {
        try {
            servicio.altaArticulo(dtoAlta);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Artículo añadido correctamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @PostMapping("/addProveedor")
    public ResponseEntity<?> addProveedor(@RequestParam Long prov, @RequestParam Long art, @RequestBody DTOArticuloProveedor dtoArticuloProveedor) {
        try {
            servicio.addProveedor(prov, art, dtoArticuloProveedor);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor añadido al artículo correctamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @PostMapping("/addProveedorPredeterminado")
    public ResponseEntity<?> addProveedorPredeterminado(@RequestParam Long prov, @RequestParam Long art) {
        try {
            servicio.setProveedorPred(prov, art);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor establecido como predeterminado de manera exitosa }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }
    /*//agregue estos endpoints para revisarlos en postman, luego hay que eliminarlos
    //calculo de Modelo Lote Fijo
    @PostMapping("/{id}/calcular-lote-optimo")
    public ResponseEntity<?> calcularLoteOptimo(@PathVariable Long id) {
        try {
            Optional<Integer> loteOptimo = servicio.calcularLoteOptimo(id);
            if (loteOptimo.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Lote óptimo calculado exitosamente\", \"loteOptimo\": " + loteOptimo.get() + "}");
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo calcular el Lote Óptimo.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al calcular el Lote Óptimo.");
        }
    }


    @PostMapping("/{id}/calcular-punto-pedido")
    public ResponseEntity<?> calcularPuntoPedido(@PathVariable Long id) {
        try {
            Optional<Integer> puntoPedido = service.calcularPuntoPedido(id);
            if (puntoPedido.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Punto Pedido calculado exitosamente\", \"punto pedido\": " + puntoPedido.get() + "}");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo calcular el Punto de Pedido.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al calcular el Punto de Pedido.");
        }
    }

    @PostMapping("/{id}/calcular-stock-seguridad")
    public ResponseEntity<?> calcularStockSeguridad(@PathVariable Long id) {
        try {
            Optional<Integer> stockSeguridad = service.calcularStockSeguridad(id);
            if (stockSeguridad.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Stock de seguridad calculado exitosamente\", \"stock seguridad\": " + stockSeguridad.get() + "}");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo calcular el Stock de Seguridad.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al calcular el Stock de Seguridad.");
        }
    }

    @PostMapping("/{idArticulo}/calcularCGI")
    public ResponseEntity<?> calcularCGI(@PathVariable Long idArticulo) {
        try {
            Optional<Double> resultado = service.calcularCGI(idArticulo);
            if (resultado.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Stock de seguridad calculado exitosamente\", \"stock seguridad\": " + resultado.get() + "}");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artículo no encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al calcular CGI.");
        }
    }*/

}
