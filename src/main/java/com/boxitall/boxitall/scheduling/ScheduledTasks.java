package com.boxitall.boxitall.scheduling;

import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraAlta;
import com.boxitall.boxitall.dtos.ordencompra.DTOOrdenCompraArticuloAlta;
import com.boxitall.boxitall.entities.Articulo;
import com.boxitall.boxitall.entities.ArticuloModeloIntervaloFijo;
import com.boxitall.boxitall.entities.Proveedor;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.services.ArticuloService;
import com.boxitall.boxitall.services.OrdenCompraService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ScheduledTasks {
    @Autowired
    private ArticuloService articuloService;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private OrdenCompraService ordenCompraService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void generarOrdenesCompra() {
        List<Articulo> articulos = articuloService.getArticulosFaltoStockIF(LocalDate.now());

        int cantOrdenes = 0;
        for (Articulo articulo: articulos) {
            Proveedor proveedorPred = articulo.getProvPred();

            if(proveedorPred == null) {
                log.info("Articulo {} no tiene proveedor predeterminado", articulo.getId());
                continue;
            }

            DTOOrdenCompraAlta dtoAltaOC = new DTOOrdenCompraAlta();
            dtoAltaOC.setIDProveedor(articulo.getProvPred().getId());

            DTOOrdenCompraArticuloAlta dtoAltaOCArt = new DTOOrdenCompraArticuloAlta();
            try {
                int cantidadPedido = articuloService.calcularCantPedidoIF(articulo);
                dtoAltaOCArt.setCantidad(cantidadPedido);
            } catch (Exception e) {
                log.info(e.getMessage());
                continue;
            }

            dtoAltaOCArt.setIDarticulo(articulo.getId());

            dtoAltaOC.setDetallesarticulo(new ArrayList<>());
            dtoAltaOC.getDetallesarticulo().add(dtoAltaOCArt);

            ordenCompraService.altaOrdenCompra(dtoAltaOC);
            cantOrdenes++;

            ArticuloModeloIntervaloFijo modeloIntervaloFijo = (ArticuloModeloIntervaloFijo) articulo.getModeloInventario();
            modeloIntervaloFijo.setFechaProximoPedido(LocalDateTime.now().plusDays(modeloIntervaloFijo.getIntervaloPedido()));
            articuloRepository.save(articulo);
        }
        log.info("De {} artículos que necesitan stock, se generaron {} OCs", articulos.size(), cantOrdenes);
    }
}