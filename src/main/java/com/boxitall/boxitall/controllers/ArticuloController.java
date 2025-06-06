package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.DTOArticuloAlta;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/IDarticulo")
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
}
