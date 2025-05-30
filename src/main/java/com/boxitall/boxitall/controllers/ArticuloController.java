package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/articulo")
public class ArticuloController extends BaseEntityControllerImpl<Articulo, ArticuloService>{

    @Autowired
    private ArticuloService servicio;

    @PostMapping("/listAll")
    public ResponseEntity<?> listAll(){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(servicio.listAll());
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Error, por favor intente más tarde\"}");
        }
    }
}
