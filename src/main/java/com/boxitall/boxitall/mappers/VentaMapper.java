package com.boxitall.boxitall.mappers;

import com.boxitall.boxitall.dtos.venta.DTOVenta;
import com.boxitall.boxitall.dtos.venta.DTOVentaAlta;
import com.boxitall.boxitall.dtos.venta.DTOVentaDetalle;
import com.boxitall.boxitall.dtos.venta.DTOVentaShort;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.Venta;
import com.boxitall.boxitall.entities.VentaDetalle;
import com.boxitall.boxitall.services.ArticuloService;
import org.hibernate.annotations.Target;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class VentaMapper {
    @Autowired
    protected ArticuloService articuloService;

    public abstract List<DTOVentaShort> ventaToDTOsVentaShort(List<Venta> venta);

    public Venta dtoVentaAltaToVenta(DTOVentaAlta dtoVentaAlta) {
        List<VentaDetalle> detalles = new ArrayList<>();
        int renglon = 0;
        try {
            for (Long articuloId : dtoVentaAlta.getArticuloIdCantidad().keySet()) {
                Articulo articulo = articuloService.findById(articuloId);
                detalles.add(new VentaDetalle(
                        dtoVentaAlta.getArticuloIdCantidad().get(articuloId),
                        renglon,
                        articulo
                ));
                renglon++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return new Venta(LocalDateTime.now(), detalles);
    }

    public abstract DTOVenta ventaToDTOVenta(Venta venta);

    @Mapping(target = "nombreArt", source = "articulo.nombre")
    @Mapping(target = "idArt", source = "articulo.id")
    public abstract DTOVentaDetalle ventaDetalleToDto(VentaDetalle ventaDetalle);

    public abstract List<DTOVentaDetalle> ventaDetalleToDtos(List<VentaDetalle> ventaDetalle);



}
