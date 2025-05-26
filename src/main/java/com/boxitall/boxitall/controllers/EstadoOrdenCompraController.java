package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.EstadoOrdenCompra;
import com.boxitall.boxitall.services.EstadoOrdenCompraService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/estado-orden-compra")
public class EstadoOrdenCompraController extends BaseEntityControllerImpl<EstadoOrdenCompra, EstadoOrdenCompraService>{
}
