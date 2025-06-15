package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompra;
import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticulo;
import com.boxitall.boxitall.dtos.venta.DTOVenta;
import com.boxitall.boxitall.dtos.venta.DTOVentaAlta;
import com.boxitall.boxitall.dtos.venta.DTOVentaDetalle;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VentaService extends BaseEntityServiceImpl<Venta, Long> {

    @Autowired
    ArticuloService articuloService;
    @Autowired
    VentaRepository repository;
    @Autowired
    OrdenCompraService ocService;
    @Autowired
    OrdenCompraRepository ordenCompraRepository;

    @Transactional
    public void altaVenta(DTOVentaAlta dto){
        try{
            List<VentaDetalle> detalles = new ArrayList<>();
            int renglon = 0; // Contador para los renglones
            for (Long articuloId : dto.getId_cantidad().keySet()){

                float cantCompra = dto.getId_cantidad().get(articuloId); // Cantidad a comprar
                Articulo articulo = articuloService.findById(articuloId);

                // Verificamos los imposibles
                if (cantCompra <= 0) throw new RuntimeException("No se pueden comprar cantidades negativas o iguales a cero");
                if (cantCompra > articulo.getStock()) throw new RuntimeException("No hay suficiente stock para la venta");

                //Bajamos el stock del artículo
                float stockActual = articulo.getStock();
                articulo.setStock(stockActual - cantCompra);

                //Checkeamos el modelo de inventario
                ArticuloModeloInventario modeloInventario = articulo.getModeloInventario();
                String modeloNombre = getModeloNombre(modeloInventario);

                //Checkeamos si el artículo tiene proveedor predeterminado
                Long provPredId = getProvPredId(articulo);

                //Checkeamos si existe una Orden de compra pendiente o enviada para el artículo
                boolean existeOCEnCurso = checkOCActivas(articulo);

                // Si es modelo de lote fijo y bajamos del punto de pedido y existe proveedor predeterminado, hacer Orden de Compra
                if (modeloNombre.equals("LoteFijo") && provPredId > 0 && !existeOCEnCurso){
                    ArticuloModeloLoteFijo modeloFijo = (ArticuloModeloLoteFijo) modeloInventario;
                    if ((stockActual - cantCompra) < modeloFijo.getPuntoPedido()){
                        DTOOrdenCompraArticulo dtoOCA = new DTOOrdenCompraArticulo(modeloFijo.getLoteOptimo(), articuloId);
                        DTOOrdenCompra dtoOC = new DTOOrdenCompra(new ArrayList<>(), provPredId);
                        dtoOC.getDetallesarticulo().add(dtoOCA);
                        ocService.altaOrdenCompra(dtoOC);
                    }
                }

                VentaDetalle detalle = new VentaDetalle(cantCompra, renglon, articulo);
                detalles.add(detalle);
                renglon++;
            }
            // Verificamos que la venta tenga al menos un detalle (al menos 1 artículo elegido)
            if (renglon == 0) throw new RuntimeException("No hay artículos seleccionados");

            Venta venta = new Venta(LocalDateTime.now(), detalles);

            repository.save(venta);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Transactional
    public DTOVenta getOne(Long id) throws Exception {
        try {
            Optional<Venta> venta = baseEntityRepository.findById(id);
            if (venta.isEmpty()) throw new RuntimeException("No se encuentra venta con ese id");
            DTOVenta dto = new DTOVenta(venta.get().getId(),venta.get().getFechaVenta(), new ArrayList<>());
            for (VentaDetalle detalle : venta.get().getDetalle()){
                DTOVentaDetalle dtoDetalle = new DTOVentaDetalle(detalle.getId(),detalle.getCantidad(),detalle.getRenglon(),detalle.getArticulo().getId(),detalle.getArticulo().getNombre());
                dto.getDetalles().add(dtoDetalle);
            }
            return dto;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<DTOVenta> getAll() throws Exception{
        try{
            List<Venta> ventas = repository.findAll();
            List<DTOVenta> dtoRetorno = new ArrayList<>();

            //Por cada venta encontrada
            for (Venta venta : ventas){
                //Un dto de venta
                DTOVenta dto = new DTOVenta(venta.getId(),venta.getFechaVenta(), new ArrayList<>());
                //Un DTODetalle por cada renglón de la venta
                for (VentaDetalle detalle : venta.getDetalle()){
                    DTOVentaDetalle dtoDetalle = new DTOVentaDetalle(detalle.getId(),detalle.getCantidad(),detalle.getRenglon(),detalle.getArticulo().getId(),detalle.getArticulo().getNombre());
                    dto.getDetalles().add(dtoDetalle);
                }
                dtoRetorno.add(dto);
            }
            return dtoRetorno;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ------ Funciones auxiliares

    // Nos da el nombre del modelo de inventario
    private String getModeloNombre(ArticuloModeloInventario modeloInventario) {
        String modeloNombre = modeloInventario.getClass().toString();
        int length = modeloNombre.length() - 1;
        int index = 0;
        for (int i = length; i > 0; i--){
            if (modeloNombre.charAt(i) == '.'){
                index = i + 1 + 14 ;  // El +1 es para que no empiece desde el punto, el + 14 para que no incluya "ArticuloModelo"
                break;
            }
        }
        modeloNombre = modeloNombre.substring(index);
        return modeloNombre;
    }

    // Nos da el ID del proveedor predeterminado o 0 si no tiene proveedor predeterminado
    private Long getProvPredId(Articulo articulo){
        if (articulo.getProvPred() == null) return 0L;
        return articulo.getProvPred().getId();
    }

    // Devuelve verdadero en caso de que exista una Orden de Compra en estado pendiente o enviada para este artículo, falso en caso contrario
    private boolean checkOCActivas(Articulo articulo){
        try{
            List<OrdenCompra> ordenesCompra = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
            return !ordenesCompra.isEmpty();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
