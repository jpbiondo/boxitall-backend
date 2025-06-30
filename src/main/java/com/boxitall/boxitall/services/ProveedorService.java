package com.boxitall.boxitall.services;


import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOAltaProveedor;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.ArticuloProveedor;
import com.boxitall.boxitall.entities.OrdenCompra;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.mappers.ArticuloMapper;
import com.boxitall.boxitall.mappers.ProveedorMapper;
import com.boxitall.boxitall.repositories.ArticuloProveedorRepository;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private ProveedorMapper proveedorMapper;

    @Autowired
    private ArticuloMapper articuloMapper;

    @Transactional
    public Proveedor altaProveedor(DTOAltaProveedor dtoAltaProveedor) throws Exception {
        try {
            List<Proveedor> proveedores = proveedorRepository.findAll();

            // Se fija que no exista otro con el mismo nombre
            for (Proveedor proveedor : proveedores) {
                if (Objects.equals(proveedor.getProveedorNombre(), dtoAltaProveedor.getNombre()) && proveedor.getProveedorFechaBaja() == null)
                    throw new RuntimeException("Ya existe un proveedor con este nombre");
            }
            Proveedor proveedor = new Proveedor();
            proveedor.setProveedorNombre(dtoAltaProveedor.getNombre());
            proveedor.setProveedorTelefono(dtoAltaProveedor.getTelefono());
            proveedor.setProveedorFechaBaja(null);

            if(dtoAltaProveedor.getProveedorArticulos().isEmpty()) {
                throw new Exception("El Proveedor debe proveer por lo menos un artículo para ser dado de alta");
            }

            proveedor = proveedorRepository.save(proveedor); // to get the id

            List<DTOArticuloAddProveedor> proveedorArticulos = dtoAltaProveedor.getProveedorArticulos();
            addArticulos(proveedorArticulos, proveedor);

            return proveedor;
        }
        catch(Exception e){
            throw new Exception("Error al dar de alta el proveedor: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<DTOProveedor> listAll(){
        try{
            List<Proveedor> proveedores = proveedorRepository.findAll();

            return proveedorMapper.proveedoresToDto(
                    proveedores.stream()
                            .filter(
                                    (proveedor ->
                                            proveedor.getProveedorFechaBaja() == null))
                            .toList());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
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

    @Transactional
    public void addArticulos(List<DTOArticuloAddProveedor> proveedorArticulos, Proveedor proveedor) {
        try {
            if (proveedorArticulos.isEmpty()) {
                throw new Exception("La lista de articulos proveedores no puede estar vacía");
            }


            for (DTOArticuloAddProveedor articuloProveedor: proveedorArticulos) {
                // Validación del artículo
                Articulo articulo = articuloService.encontrarArticulo(articuloProveedor.getArticuloId());
                articuloService.checkBaja(articulo);
                if (proveedorYaProveeArticulo(articulo.getArtProveedores(), proveedor)) {
                    throw new Exception("El proveedor ya existe para este artículo");
                }

                // Preparación del articuloProveedor
                ArticuloProveedor artProv = articuloMapper.dtoAltaProvArtToArtProv(articuloProveedor);
                artProv.setProveedor(proveedor);
                System.out.println(artProv.toString());
                articulo.getArtProveedores().add(artProv);

                articuloService.update(articulo.getId(), articulo);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean proveedorYaProveeArticulo(List<ArticuloProveedor> articuloProveedores, Proveedor proveedor) {
        List<ArticuloProveedor> artsDelProv = articuloProveedores.stream().filter(
                (artProv) ->
                        artProv.getProveedor().getId()
                                .equals(proveedor.getId())).toList();

        return !artsDelProv.isEmpty();
    }

}
