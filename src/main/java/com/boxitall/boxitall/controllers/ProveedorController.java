package com.boxitall.boxitall.controllers;


import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.services.ProveedorService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/proveedor")
public class ProveedorController extends BaseEntityControllerImpl<Proveedor, ProveedorService>{
}