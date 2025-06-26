package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraAlta;
import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticuloAlta;
import com.boxitall.boxitall.dtos.venta.DTOVenta;
import com.boxitall.boxitall.dtos.venta.DTOVentaAlta;
import com.boxitall.boxitall.dtos.venta.DTOVentaDetalle;
import com.boxitall.boxitall.dtos.venta.DTOVentaShort;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.mappers.VentaMapper;
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
    @Autowired
    VentaMapper ventaMapper;

    @Transactional
    public void altaVenta(DTOVentaAlta dto){
        try{
            Venta venta = ventaMapper.dtoVentaAltaToVenta(dto);

            Articulo articulo = null;
            float cantCompra = 0;
            for (VentaDetalle detalle: venta.getDetalle())
            {
                articulo = detalle.getArticulo();
                cantCompra = detalle.getCantidad();

                if (articulo == null) {
                    throw new RuntimeException("El artículo no puede ser nulo");
                }

                //Verificamos que el artículo sea comprable (no de baja y con proveedor predeterminado)
                if (articulo.getProvPred() == null) throw new RuntimeException("El artículo no está listo para ser vendido al no tener proveedor predeterminado");
                if (articulo.getFechaBaja() != null) throw new RuntimeException("El artíuclo está dado de baja y no puede ser vendido");

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
                        DTOOrdenCompraArticuloAlta dtoOCA = new DTOOrdenCompraArticuloAlta(modeloFijo.getLoteOptimo(), articulo.getId());
                        DTOOrdenCompraAlta dtoOC = new DTOOrdenCompraAlta(new ArrayList<>(), provPredId);
                        dtoOC.getDetallesarticulo().add(dtoOCA);
                        ocService.altaOrdenCompra(dtoOC);
                    }
                }
            }
            // Verificamos que la venta tenga al menos un detalle (al menos 1 artículo elegido)
            if (venta.getDetalle().isEmpty()) throw new RuntimeException("No hay artículos seleccionados");
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

            return ventaMapper.ventaToDTOVenta(venta.get());

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public List<DTOVentaShort> getAll() throws Exception{
        try {
            List<Venta> ventas = repository.findAll();
            return ventaMapper.ventaToDTOsVentaShort(ventas);
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
