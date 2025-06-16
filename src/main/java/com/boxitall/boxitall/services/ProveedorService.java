package com.boxitall.boxitall.services;


import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOAltaProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.ArticuloProveedorRepository;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
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

    @Autowired
    private ArticuloProveedorRepository articuloProveedorRepository;

    @Transactional

    public Proveedor altaProveedor(DTOAltaProveedor dtoAltaProveedor) throws Exception {
        try {
            DTOProveedor dtoProveedor = dtoAltaProveedor.getDtoProveedor();
            DTOArticuloAddProveedor dtoArticuloProveedor = dtoAltaProveedor.getDtoArticuloAddProveedor();

            if (proveedorRepository.existsByProveedorCod((dtoProveedor.getProveedorCod()))){
                throw new Exception("El proveedor con codigo " + dtoProveedor.getProveedorCod() + " ya está registrado.");
            }
            // Crear un nuevo proveedor
            Proveedor proveedor = new Proveedor(
                    dtoProveedor.getProveedorCod(),
                    dtoProveedor.getProveedorNombre(),
                    dtoProveedor.getProveedorTelefono(),
                    dtoProveedor.getProveedorFechaBaja()
            );

            Proveedor savedProveedor = proveedorRepository.save(proveedor);
            // Asegurarse de que el proveedor esté asociado a al menos un artículo
            articuloService.addProveedor(dtoArticuloProveedor);
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
                List<OrdenCompra> ordenesActivas = ordenCompraRepository.findOrdenesActivasbyProveedor(proveedor);
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
    /*public List<Articulo> obtenerArticulosPorProveedor(Long idProveedor) {
        // Obtener el proveedor desde la base de datos
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // Obtener todos los artículos asociados con este proveedor
        return articuloProveedorRepository.findArticulosByProveedorId(proveedor.getId());
    }*/
}
