package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.articulo.*;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.List;
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @GetMapping("/bajados")
    public ResponseEntity<?> bajados() {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(servicio.bajados());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
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
    public ResponseEntity<?> addProveedor(@RequestBody DTOArticuloAddProveedor dtoAddProveedor){
        try{
            servicio.addProveedor(dtoAddProveedor);
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

    @DeleteMapping("/quitarProveedor")
    public ResponseEntity<?> quitarProveedor(@RequestParam Long proveedorId, @RequestParam Long articuloId){
        try{
            servicio.quitarProveedor(proveedorId,articuloId);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor quitado para el artículo ingresado }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @DeleteMapping("/bajaArticulo")
    public ResponseEntity<?> bajaArticulo(@RequestParam Long id){
        try{
            servicio.bajaArticulo(id);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor quitado para el artículo ingresado }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }
    @GetMapping("/listarPorProveedor")
    public ResponseEntity<?> listarArticulosAgrupadosPorProveedor() {
        try {
            Map<String, List<DTOArticuloProveedorListado>> resultado = servicio.listarArticulosPorProveedor();
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error al listar artículos por proveedor: " + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/listarPorProveedorId")
    public ResponseEntity<?> listarArticulosPorProveedorId(@RequestParam Long idProveedor) {
        try {
            List<DTOArticuloProveedorListado> articulos = servicio.listarArticulosPorProveedorId(idProveedor);
            return ResponseEntity.status(HttpStatus.OK).body(articulos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Error al listar artículos del proveedor: " + e.getMessage() + "\"}");
        }
    }

    //Listado Productos a reponer
    @GetMapping("/listarProductosAReponer")
    public ResponseEntity<?> listarProductosAReponer() {
        try {
            List<DTOArticuloListado> productos = servicio.listarProductosAReponer();
            return ResponseEntity.status(HttpStatus.OK).body(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    //Listado productos faltantes
    @GetMapping("/listarProductosFaltantes")
    public ResponseEntity<?> listarProductosFaltantes() {
        try {
            List<DTOArticuloListado> productos = servicio.listarProductosFaltantes();
            return ResponseEntity.status(HttpStatus.OK).body(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    //Listado proveedores por articulo
    @GetMapping("/listarProveedoresPorArticulo")
    public ResponseEntity<?> listarProveedoresPorArticulo(@RequestParam Long articuloId) {
        try {
            List<DTOProveedor> proveedores = servicio.listarProveedoresPorArticulo(articuloId);
            return ResponseEntity.status(HttpStatus.OK).body(proveedores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    //Ajuste de inventario
    @PutMapping("/ajustarInventario")
    public ResponseEntity<?> ajustarInventario(@RequestParam Long articuloId, @RequestParam float nuevaCantidad) {
        try {
            servicio.ajustarInventario(articuloId, nuevaCantidad);
            return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Inventario ajustado correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }




    /* lo deje porque no se bien como lo vamos a implementar definitivamente
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
