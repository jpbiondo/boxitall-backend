package com.boxitall.boxitall.services;


import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService extends BaseEntityServiceImpl<Proveedor, Long> {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ArticuloService articuloService;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    public Proveedor altaProveedor(Proveedor proveedor, Long idArt) throws Exception {
        try {
            if (proveedorRepository.existsById(proveedor.getId())) {
                throw new Exception("El proveedor con ID " + proveedor.getId() + " ya está registrado.");
            }
            Proveedor savedProveedor = proveedorRepository.save(proveedor);
            // Asegurarse de que el proveedor esté asociado a al menos un artículo
            // TODO - faltan los datos del artículoProveedor en el addProveedor. Le pongo valores base por ahora
            DTOArticuloAddProveedor dtoAddProveedor = new DTOArticuloAddProveedor(idArt, savedProveedor.getId(), 0f,0f,0,0,0,0);
            articuloService.addProveedor(dtoAddProveedor);
            return savedProveedor;
        }
        catch(Exception e){
            throw new Exception("Error al dar de alta el proveedor: " + e.getMessage(), e);
        }


    }
    @Override
    public boolean delete(Long id) throws Exception {
        try {
            if (proveedorRepository.existsById(id)){
                Optional<Proveedor> proveedorOptional = proveedorRepository.findById(id);
                if (proveedorOptional.isEmpty()) {
                    throw new Exception("Proveedor no encontrado.");
                }

                Proveedor proveedor = proveedorOptional.get();

                // Verifica si el proveedor es el predeterminado
                List<Articulo> articulos = articuloRepository.findByProvPred(proveedor);
                if (!articulos.isEmpty()) {
                    throw new Exception("No se puede dar de baja el proveedor porque es el proveedor predeterminado de algunos artículos.");
                }
                // Verifica si el proveedor tiene una orden de compra pendiente o en curso
                List<OrdenCompra> ordenesActivas = ordenCompraRepository.findOrdenesActivasByProveedor(proveedor);
                if (!ordenesActivas.isEmpty()) {
                    throw new Exception("El proveedor tiene órdenes de compra activas (pendientes o enviadas) y no puede ser dado de baja.");
                }
                proveedor.setProveedorFechaBaja(LocalDateTime.now());
                proveedorRepository.save(proveedor);
                return true;
            }else {
                throw new Exception("No se encontró el proveedor con id "+id+" para dar de baja");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}
