package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.Venta;
import com.boxitall.boxitall.services.VentaService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/venta")
public class VentaController extends BaseEntityControllerImpl<Venta, VentaService> {
}
