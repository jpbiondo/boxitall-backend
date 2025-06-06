package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.venta.DTOVenta;
import com.boxitall.boxitall.dtos.venta.DTOVentaAlta;
import com.boxitall.boxitall.dtos.venta.DTOVentaDetalle;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Venta;
import com.boxitall.boxitall.entities.VentaDetalle;
import com.boxitall.boxitall.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VentaService extends BaseEntityServiceImpl<Venta, Long> {

    @Autowired
    ArticuloService articuloService;
    @Autowired
    VentaRepository repository;

    @Transactional
    public void altaVenta(DTOVentaAlta dto){
        try{
            Venta venta = new Venta();
            venta.setFechaVenta(new Date());

            List<VentaDetalle> detalles = new ArrayList<>();
            int i = 0; // Contador para los renglones
            for (Long articuloId : dto.getId_cantidad().keySet()){

                float cantCompra = dto.getId_cantidad().get(articuloId); // Cantidad a comprar
                Articulo articulo = articuloService.findById(articuloId);

                // Verificamos los imposibles
                if (cantCompra <= 0) throw new RuntimeException("No se pueden comprar cantidades negativas o iguales a cero");
                if (cantCompra > articulo.getStock()) throw new RuntimeException("No hay suficiente stock para la venta");

                //Bajamos el stock del artículo
                float stockActual = articulo.getStock();
                articulo.setStock(stockActual - cantCompra);

                // TODO - Hacer la Orden de Compra

                VentaDetalle detalle = new VentaDetalle(cantCompra, i, articulo, venta);
                detalles.add(detalle);
                i++;
            }
            // Verificamos que la venta tenga al menos un detalle (al menos 1 artículo elegido)
            if (i == 0) throw new RuntimeException("No hay artículos seleccionados");

            venta.setDetalle(detalles);
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
}
