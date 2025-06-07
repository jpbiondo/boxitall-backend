package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAlta;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloDetalle;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloListado;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
public class ArticuloService extends BaseEntityServiceImpl<Articulo, Long> {
    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Transactional
    public void altaArticulo(DTOArticuloAlta dto){
        try{
            List<Articulo> articulos = articuloRepository.findAll(); //Encuentra todos los artículos
            for (Articulo articulo : articulos){
                System.out.println(articulo.getNombre() + " y " + dto.getNombre());
                if (Objects.equals(articulo.getNombre(), dto.getNombre()))
                    throw new RuntimeException("Ya existe un artículo con este nombre");
            }

            //Decidir modelo de inventario
            ArticuloModeloInventario modeloInventario;
            switch (dto.getModeloNombre()){
                case "LoteFijo" -> {
                    modeloInventario = new ArticuloModeloLoteFijo(dto.getLoteOptimo(), dto.getPuntoPedido());
                }
                case "IntervaloFijo" ->{
                    modeloInventario = new ArticuloModeloIntervaloFijo(LocalDate.now().plusDays(dto.getIntervaloPedido()) , dto.getIntervaloPedido(), dto.getInventarioMaximo());
                }
                default -> throw new RuntimeException("Modelo desconocido");
            }

            // Crear artículo
            Articulo articulo = new Articulo(
                    dto.getNombre(), dto.getDescripcion(),dto.getCostoAlmacenamiento(),
                    dto.getDemanda(),dto.getDemandaDesviacionEstandar(),dto.getNivelServicio(),
                    dto.getStock(), modeloInventario          // TODO - Proveedor o estado?
            );

            // Guardar el artículo
            articuloRepository.save(articulo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public List<DTOArticuloListado> listAll(){
        try{
            List<Articulo> articulos = articuloRepository.findAll(); //Encuentra todos los artículos
            List<DTOArticuloListado> dtos = new ArrayList<>(); //Crea el array de respuesta

            // Por cada artículo vamos a crear un DTO que agregamos al array de respuesta
            for(Articulo articulo : articulos){
                DTOArticuloListado dto = crearDtoListado(articulo); // Hacemos el dto
                dtos.add(dto); // Agregamos el dto al array de respuesta
            }
            return dtos;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void bajaArticulo(Long id){         // TODO - Baja con estado?

    }

    // Obtiene toda la información del artículo para mostrarla en detalle
    @Transactional
    public DTOArticuloDetalle getArticuloDetalle(Long id){
        try{
            //Encontrar artículo
            Articulo articulo = encontrarArticulo(id);

            DTOArticuloDetalle dto = new DTOArticuloDetalle(
                    articulo.getId(), articulo.getNombre(), articulo.getStock(), articulo.getDescripcion(), articulo.getCostoAlmacenamiento(),
                    articulo.getModeloInventario().getNombre(), new Date(), 0f, // TODO - Modelo Inventario
                    articulo.getProvPred().getId(), articulo.getProvPred().getProveedorNombre(),
                    10,10,10,10,10 // TODO - CGI
            );
            return dto;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void addProveedor(Long idProveedor, Long idArt){
        try{
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(idArt);
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(idProveedor);

            //checkear que no esté ya agregado el proveedor
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            if (artProvs != null){
                for(ArticuloProveedor artProv: artProvs){
                    if (artProv.getProveedor() == proveedor){
                        throw new Exception("El proveedor ya existe para este artículo");
                    }
                }
            }

            // Agregar ArtículoProveedor
            ArticuloProveedor artProv = new ArticuloProveedor(); //TODO atributos ArticuloProveedor
            artProv.setProveedor(proveedor);
            artProvs.add(artProv);
            articulo.setArtProveedores(artProvs);

            // Guardar cambios
            update(idArt, articulo);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void setProveedorPred(Long idProveedor, Long idArt){
        try{
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(idArt);
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(idProveedor);         // TODO - Checkear esta línea y la siguiente

            //checkear que ya esté agregado el proveedor
            boolean provee = false;
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            for(ArticuloProveedor artProv: artProvs){
                if (artProv.getProveedor() == proveedor){
                    provee = true;
                    break;
                }
            }
            if (!provee) throw new Exception("El proveedor ingresado no provee este artículo");

            if (articulo.getProvPred() == proveedor) throw new RuntimeException("El proveedor ingresado ya es proveedor predeterminado de este artículo"); // TODO - Better handling

            //Settear proveedor
            articulo.setProvPred(proveedor);

            // TODO RECALCULAR CGI? Creo que no
            // TODO RECALCULAR lote óptimo, punto pedido, stock seguridad

            //Guardar cambios
            update(idArt, articulo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Encuentra un artículo que puede o no estar
    private Articulo encontrarArticulo(Long idArt) throws Exception {
        Optional<Articulo> optArticulo = articuloRepository.findById(idArt);
        if (optArticulo.isEmpty()) throw new Exception("No se encuentra el artículo");
        return optArticulo.get();
    }

    // Encuentra un proveedor que puede o no estar
    private Proveedor encontrarProveedor(Long idProv) throws Exception {
        Optional<Proveedor> optProveedor = proveedorRepository.findById(idProv);
        if (optProveedor.isEmpty()) throw new Exception("No se encuentra el proveedor");
        return optProveedor.get();
    }

    private DTOArticuloListado crearDtoListado(Articulo articulo){
        Long provPredId;
        String provPredNom;

        //Chequeamos que el proveedor predeterminado exista
        if (articulo.getProvPred() == null){
            provPredId = 0L;
            provPredNom = "Sin proveedor predeterminado";
        } else {
            provPredId = articulo.getProvPred().getId();
            provPredNom = articulo.getProvPred().getProveedorNombre();
        }

        // Creamos el dto en sí
        DTOArticuloListado dto = new DTOArticuloListado(
                articulo.getId(), articulo.getNombre(), articulo.getStock(),
                "modelo prueba", new Date(), 0, // TODO - Modelo inventario
                provPredId, provPredNom
        );
        return dto;
    }
}
