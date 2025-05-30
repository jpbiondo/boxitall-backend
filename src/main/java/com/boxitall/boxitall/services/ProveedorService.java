package com.boxitall.boxitall.services;


import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class ProveedorService extends BaseEntityServiceImpl<Proveedor, Long>{

    @Autowired
    private ProveedorRepository proveedorRepository;

    public boolean alta(Long id) throws Exception {
        try {
            if (ProveedorRepository.existsById(proveedorRepository.getId())) {
                // Si el proveedor ya existe, lanzamos una excepción
                throw new Exception("El proveedor con id " + proveedor.getId() + " ya está registrado."); }

            proveedorRepository.save(proveedor);
            return true;
            };
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
