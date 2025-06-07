package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAlta;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/articulo")
public class ArticuloController extends BaseEntityControllerImpl<Articulo, ArticuloService>{

    @Autowired
    private ArticuloService servicio;

    @GetMapping("/listAll")
    public ResponseEntity<?> listAll(){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(servicio.listAll());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente más tarde\"}");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addArticle(@RequestBody DTOArticuloAlta dtoAlta){
        try{
            servicio.altaArticulo(dtoAlta);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Artículo añadido correctamente }\"");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @PostMapping("/addProveedor")
    public ResponseEntity<?> addProveedor(@RequestParam Long prov, @RequestParam Long art){
        try{
            servicio.addProveedor(prov,art);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor añadido al artículo correctamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @PostMapping("/addProveedorPredeterminado{idProveedor}{idArticulo}")
    public ResponseEntity<?> addProveedorPredeterminado(@RequestParam Long prov, @RequestParam Long art){
        try{
            servicio.setProveedorPred(prov,art);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Proveedor establecido como predeterminado de manera exitosa }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }
}
