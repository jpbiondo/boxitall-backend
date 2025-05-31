package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.DTOArticuloDetalle;
import com.boxitall.boxitall.dtos.DTOArticuloListado;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.ArticuloProveedor;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
    TODO

     LISTALL
        Info proveedor
        Modelo de inventario
            Qué dato mostrar según modelo inventario
     GETARTICULO DETALLE
        Info Proveedor pred, modelo de invenatrio, proveedores
 */

@Service
public class ArticuloService extends BaseEntityServiceImpl<Articulo, Long> {
    @Autowired
    private ArticuloRepository articuloRepository;

    @Transactional
    public List<DTOArticuloListado> listAll(){
        try{
            List<Articulo> articulos = articuloRepository.findAll();
            List<DTOArticuloListado> dtos = new ArrayList<>();
            for(Articulo articulo : articulos){
                DTOArticuloListado dto = new DTOArticuloListado(
                        articulo.getId(),
                        articulo.getNombre(),
                        articulo.getStock(),
                        "modelo prueba", //Cambiar
                        new Date(), // Cambiar
                        0, // Cambiar
                        "Roberto", // Cambiar
                        20L // Cambiar
                );
                dtos.add(dto);
            }
            return dtos;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public DTOArticuloDetalle getArticuloDetalle(Long id){
        try{
            //Encontrar artículo
            Articulo articulo = encontrarArticulo(id);

            DTOArticuloDetalle dto = new DTOArticuloDetalle(
                    articulo.getId(), articulo.getNombre(), articulo.getStock(), articulo.getDescripcion(), articulo.getCostoAlmacenamiento(),
                    "modInv", new Date(), 0f, // Cambiar - Modelo Inventario
                    10L, "Roberto", // Cambiar - Proveedor
                    10,10,10,10,10 //Cambiar - CGI
            );
            return dto;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void addProveedor(Proveedor proveedor, Long idArt){
        try{
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(idArt);

            //checkear que no esté ya agregado el proveedor
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            for(ArticuloProveedor artProv: artProvs){
                if (artProv.getProveedor() == proveedor){
                    throw new Exception("El proveedor ya existe para este artículo");
                }
            }

            // Agregar ArtículoProveedor
            ArticuloProveedor artProv = new ArticuloProveedor(proveedor);
            artProvs.add(artProv);
            articulo.setArtProveedores(artProvs);

            // Guardar cambios
            update(idArt, articulo);
        }
        catch (Exception e) {

        }
    }

    @Transactional
    public void setProveedorPred(Proveedor proveedor, Long idArt){
        try{
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(idArt);

            //checkear que ya esté agregado el proveedor
            boolean presente = false;
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            for(ArticuloProveedor artProv: artProvs){
                if (artProv.getProveedor() == proveedor){
                    presente = true;
                    break;
                }
            }
            if (!presente) throw new Exception("El proveedor ingresado no provee este artículo");

            //Settear proveedor
            articulo.setProvPred(proveedor);

            //RECALCULAR CGI?
            //RECALCULAR lote óptimo, punto pedido, stock seguridad

            //Guardar cambios
            update(idArt, articulo);
        } catch (Exception e) {

        }
    }

    // Encuentra un artículo que puede o no estar
    private Articulo encontrarArticulo(Long idArt) throws Exception {
        Optional<Articulo> optArticulo = articuloRepository.findById(idArt);
        if (optArticulo.isEmpty()) throw new Exception("No se encuentra el artículo");
        return optArticulo.get();
    }
}
