package com.boxitall.boxitall.controllers;

import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.services.ArticuloService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/articulo")
public class ArticuloController extends BaseEntityControllerImpl<Articulo, ArticuloService>{
}
