package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.dtos.venta.DTOVentaAlta;
import com.boxitall.boxitall.entities.Venta;
import com.boxitall.boxitall.services.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/venta")
public class VentaController extends BaseEntityControllerImpl<Venta, VentaService> {

    @Autowired
    VentaService service;

    @PostMapping("/altaVenta")
    public ResponseEntity<?> altaVenta(@RequestBody DTOVentaAlta dto){
        try{
            service.altaVenta(dto);
            return ResponseEntity.status(HttpStatus.OK).body("{\" Venta agregada exitosamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\" error\":\"Error," + e.getMessage() + "}\"");
        }
    }

    @Override
    @GetMapping("/getOne")
    public ResponseEntity<?> getOne(@RequestParam Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.getOne(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("\"error\":\"Error, por favor intente más tarde.\"}");
        }
    }

    @Override
    public ResponseEntity<?> getAll(){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(service.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("\"error\":\"No se encuentran ventas.\"}");
        }
    }

}
