package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAlta;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloDetalle;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloListado;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                    LocalDateTime proxPedido = LocalDateTime.now().plusDays(dto.getIntervaloPedido());
                    modeloInventario = new ArticuloModeloIntervaloFijo(proxPedido , dto.getIntervaloPedido(), dto.getInventarioMaximo());
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

            //Obtener la info del modelo de inventario
            MiniDTOModeloInventario miniDTO = datosModeloInventario(articulo.getModeloInventario());
            MiniDTOProvPred miniDTOProvPred = datosProvPred(articulo);

            DTOArticuloDetalle dto = new DTOArticuloDetalle(
                    articulo.getId(), articulo.getNombre(), articulo.getStock(), articulo.getDescripcion(), articulo.getCostoAlmacenamiento(),
                    miniDTO.getModeloNombre(), miniDTO.getFechaProxPedido(), miniDTO.getCantProxPedido(),
                    miniDTOProvPred.getProvId(), miniDTOProvPred.getProvNombre(),
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
            else artProvs = new ArrayList<>();      // Está por un warning que tiraba, pero andaba igual con o sin

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
            Proveedor proveedor = encontrarProveedor(idProveedor);

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
            throw new RuntimeException("No se pudo establecer el proveedor predeterminado");
        }
    }

    // -------- Funciones auxiliares

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

    //Arma un DTO de listado (tiene menos info)
    private DTOArticuloListado crearDtoListado(Articulo articulo){

        // Obtenemos el nombre del modelo, la fecha (estimada) y la cantidad (estimada) del próximo pedido
        MiniDTOModeloInventario miniDTOModelo = datosModeloInventario(articulo.getModeloInventario());
        MiniDTOProvPred miniDTOProvPred = datosProvPred(articulo);

        // Creamos el dto en sí
        DTOArticuloListado dto = new DTOArticuloListado(
                articulo.getId(), articulo.getNombre(), articulo.getStock(),
                miniDTOModelo.getModeloNombre(), miniDTOModelo.getFechaProxPedido() , miniDTOModelo.getCantProxPedido(),
                miniDTOProvPred.getProvId(), miniDTOProvPred.getProvNombre()
        );
        return dto;
    }

    //Un mini DTO del proveedor predeterminado para usar dentro de este service
    @Setter
    @Getter
    private class MiniDTOProvPred {
        private Long provId;
        private String provNombre;
    }

    // Retorna el id y nombre del proveedor predeterminado, si es que existe
    private MiniDTOProvPred datosProvPred(Articulo articulo){
        MiniDTOProvPred dto = new MiniDTOProvPred();
        Long provPredId = 0L;
        String provPredNom = "No hay proveedor predeterminado";

        // Chequeamos que el proveedor predeterminado exista
        if (articulo.getProvPred() == null){
            provPredId = 0L;
            provPredNom = "Sin proveedor predeterminado";
        } else {
            provPredId = articulo.getProvPred().getId();
            provPredNom = articulo.getProvPred().getProveedorNombre();
        }

        dto.setProvId(provPredId);
        dto.setProvNombre(provPredNom);

        return dto;
    }

    //Un mini DTO del modelo de inventario para usar dentro de este service
    @Setter
    @Getter
    private class MiniDTOModeloInventario {
        private String modeloNombre;
        private LocalDateTime fechaProxPedido; // Fecha concreta o estimada
        private float cantProxPedido; // Cantidad a pedir, concreta o estimada
    }

    // Retorna el nombre del modelo, la fecha (estimada) y la cantidad (estimada) del próximo pedido
    private MiniDTOModeloInventario datosModeloInventario(ArticuloModeloInventario modeloInventario){
        MiniDTOModeloInventario dto = new MiniDTOModeloInventario();

        // Obtener el nombre del modelo
        String modeloNombre = modeloInventario.getClass().toString();
        int length = modeloNombre.length() - 1;
        int index = 0;
        for (int i = length; i > 0; i--){
            if (modeloNombre.charAt(i) == '.'){
                index = i + 1 + 14 ;  // El +1 es para que no empiece desde el punto, el + 14 para que no incluya "ArticuloModelo"
                break;
            }
        }
        // Settear el nombre del modelo
        modeloNombre = modeloNombre.substring(index);
        dto.setModeloNombre(modeloNombre);

        // Obtener la fecha y stock de próximo pedido
        LocalDateTime proxPedido;
        float stockPedido;
        switch (modeloNombre){
            case "LoteFijo" -> {
                ArticuloModeloLoteFijo modeloEspecifico = (ArticuloModeloLoteFijo) modeloInventario;
                proxPedido = LocalDateTime.now();           // TODO - Fecha según la demanda estimada
                stockPedido = modeloEspecifico.getLoteOptimo();         // TODO - No sé si es esto, temporal
            }
            case "IntervaloFijo" -> {
                ArticuloModeloIntervaloFijo modeloEspecifico = (ArticuloModeloIntervaloFijo) modeloInventario;
                proxPedido = modeloEspecifico.getFechaProximoPedido();
                stockPedido = 20f;          // TODO - Calcular el estimado
            }
            default -> throw new RuntimeException("El artículo no posee modelo de inventario");
        }
        //Settear la fecha y stock del próximo pedido
        dto.setFechaProxPedido(proxPedido);
        dto.setCantProxPedido(stockPedido);

        return dto;
    }

}
