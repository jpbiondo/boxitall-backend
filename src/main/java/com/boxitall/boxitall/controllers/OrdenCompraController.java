package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.services.OrdenCompraService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/orden-compra")
public class OrdenCompraController extends BaseEntityControllerImpl<OrdenCompra, OrdenCompraService>{
}
