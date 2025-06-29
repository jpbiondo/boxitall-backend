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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bajados")
    public ResponseEntity<?> bajados() {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(servicio.bajados());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/getDetalles")
    public ResponseEntity<?> getDetalles(@RequestParam Long id) {
        try {
            DTOArticuloDetalle dtoDetalle = servicio.getArticuloDetalle(id);
            return ResponseEntity.status(HttpStatus.OK).body(dtoDetalle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addArticle(@RequestBody DTOArticuloAlta dtoAlta) {
        try {
            servicio.altaArticulo(dtoAlta);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Artículo añadido correctamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/updateArticulo")
    public ResponseEntity<?> updateArticulo(@RequestParam Long id, @RequestBody DTOArticuloAlta dtoArticuloAlta){
        try{
            servicio.updateArticulo(id, dtoArticuloAlta);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Artículo actualizado correctamente }\"");

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/bajaArticulo")
    public ResponseEntity<?> bajaArticulo(@RequestParam Long id){
        try{
            servicio.bajaArticulo(id);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor quitado para el artículo ingresado }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/listarPorProveedor")
    public ResponseEntity<?> listarArticulosAgrupadosPorProveedor() {
        try {
            List<DTOArticuloGrupoProveedor> resultado = servicio.listarArticulosPorProveedor();
            return ResponseEntity.status(HttpStatus.OK).body(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/listarPorProveedorId")
    public ResponseEntity<?> listarArticulosPorProveedorId(@RequestParam Long idProveedor) {
        try {
            List<DTOArticuloProveedorListado> articulos = servicio.listarArticulosPorProveedorId(idProveedor);
            return ResponseEntity.status(HttpStatus.OK).body(articulos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    //Listado Productos a reponer
    @GetMapping("/listarProductosAReponer")
    public ResponseEntity<?> listarProductosAReponer() {
        try {
            List<DTOArticuloListado> productos = servicio.listarProductosAReponer();
            return ResponseEntity.status(HttpStatus.OK).body(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    //Listado productos faltantes
    @GetMapping("/listarProductosFaltantes")
    public ResponseEntity<?> listarProductosFaltantes() {
        try {
            List<DTOArticuloFaltante> productos = servicio.listarProductosFaltantes();
            return ResponseEntity.status(HttpStatus.OK).body(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    //Listado proveedores por articulo
    @GetMapping("/listarProveedoresPorArticulo")
    public ResponseEntity<?> listarProveedoresPorArticulo(@RequestParam Long articuloId) {
        try {
            List<DTOProveedor> proveedores = servicio.listarProveedoresPorArticulo(articuloId);
            return ResponseEntity.status(HttpStatus.OK).body(proveedores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    //Ajuste de inventario
    @PutMapping("/ajustarInventario")
    public ResponseEntity<?> ajustarInventario(@RequestParam Long articuloId, @RequestParam float nuevaCantidad) {
        try {
            servicio.ajustarInventario(articuloId, nuevaCantidad);
            return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"Inventario ajustado correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

}
