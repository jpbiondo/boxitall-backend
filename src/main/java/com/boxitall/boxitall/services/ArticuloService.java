package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.articulo.*;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.dtos.articulo.*;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.mappers.ArticuloMapper;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.distribution.NormalDistribution;
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

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ArticuloMapper articuloMapper;

    @Transactional
    public void altaArticulo(DTOArticuloAlta dto) {
        try {
            List<Articulo> articulos = articuloRepository.findAll(); //Encuentra todos los artículos

            // Se fija que no exista otro con el mismo nombre
            for (Articulo articulo : articulos) {
                if (Objects.equals(articulo.getNombre(), dto.getNombre()) && articulo.getFechaBaja() == null)
                    throw new RuntimeException("Ya existe un artículo con este nombre");
            }

            //Creamos el artículo con su modelo sus atributos propios y del modelo de inventario
            Articulo articulo = new Articulo();
            commonAltaUpdate(articulo, dto);

            // Agregar los proveedores
            for (DTOArticuloAddProveedor dtoArtProv : dto.getArticuloProveedores()){
                addProveedor(articulo, dtoArtProv);
            }

            // Agregar prov pred
            setProveedorPred(articulo, dto.getProveedorPredeterminadoId());

            // Guardar el artículo
            articuloRepository.save(articulo);

        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updateArticulo(Long artId, DTOArticuloAlta dto){
        try{
            Articulo articulo = encontrarArticulo(artId);

            checkBaja(articulo);

            // Ponemos la info del art en sí, y del modelo nuevo
            commonAltaUpdate(articulo, dto);

            // Agrega / cambia todos los artProv según lo que llegó en el dto
            loopDTOs: for (DTOArticuloAddProveedor dtoArtProv : dto.getArticuloProveedores()){
                for (ArticuloProveedor artProv : articulo.getArtProveedores()){
                    if (artProv.getProveedor().getId() == dtoArtProv.getProveedorId()){         // Lo que sea igual lo actualiza
                        artProv.setPrecioUnitario(dtoArtProv.getPrecioUnitario());
                        artProv.setDemoraEntrega(dtoArtProv.getDemoraEntrega());
                        artProv.setCargoPedido(dtoArtProv.getCargoPedido());
                        artProv.setCostoCompra(dtoArtProv.getCostoCompra());
                        artProv.setCostoPedido(dtoArtProv.getCostoPedido());
                        continue loopDTOs;
                    }
                }
                // No había artProv para ese dto, se agrega un artProv
                dtoArtProv.setArticuloId(articulo.getId());
                addProveedor(articulo, dtoArtProv);
            }

            // Este array son los artProv que ya tenía el artículo + los nuevos
            List<ArticuloProveedor> newArtProvs = new ArrayList<>(articulo.getArtProveedores());

            // Loopeamos de vuelta para eliminar los artProv que ya no estén
            loopExistentes: for (ArticuloProveedor artProv : articulo.getArtProveedores()){
                for (DTOArticuloAddProveedor dtoArtProv : dto.getArticuloProveedores()){
                    if (artProv.getProveedor().getId() == dtoArtProv.getProveedorId()){
                        //Ponemos al nuevo proveedor predeterminado
                        if (dtoArtProv.getProveedorId() == dto.getProveedorPredeterminadoId())
                            setProveedorPred(articulo, dto.getProveedorPredeterminadoId());
                        continue loopExistentes;
                    }
                }
                // No había dtoArtProv que fuera igual, por lo tanto debería ser eliminado
                // Antes, chequear que no tenga órdenes de compra para ese art
                if (!checkOredenesActivasArtProv(articulo, artProv.getProveedor()))
                    newArtProvs.remove(artProv);
                else
                    throw new RuntimeException("Se está intentando eliminar un proveedor con una orden de compra activa. Finalizarla primero");
            }

            // Ponemos el array corregido como el nuevo ArtProveedores
            articulo.setArtProveedores(newArtProvs);

            // Agregar/ cambiar prov pred
            setProveedorPred(articulo, dto.getProveedorPredeterminadoId());

            update(articulo.getId(), articulo);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public List<DTOArticuloBajado> bajados(){
        List<Articulo> articulos = articuloRepository.findByBajado();
        List<DTOArticuloBajado> dtos = new ArrayList<>();
        if (articulos.isEmpty())
            throw new RuntimeException("No hay artículos dados de baja");
        for (Articulo articulo: articulos){
            DTOArticuloBajado dto = new DTOArticuloBajado(
                    articulo.getId(), articulo.getNombre(), articulo.getDescripcion(),
                    articulo.getCostoAlmacenamiento(), articulo.getNivelServicio(),
                    articulo.getFechaBaja()
                    );
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public List<DTOArticuloListado> listAll() {
        try {
            List<Articulo> articulos = articuloRepository.findAll(); //Encuentra todos los artículos
            List<DTOArticuloListado> dtos = new ArrayList<>(); //Crea el array de respuesta

            // Por cada artículo vamos a crear un DTO que agregamos al array de respuesta
            for(Articulo articulo : articulos){
                if (articulo.getFechaBaja() == null){
                    DTOArticuloListado dto = crearDtoListado(articulo); // Hacemos el dto
                    dtos.add(dto); // Agregamos el dto al array de respuesta//
                }
            }
            return dtos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Da de baja el artículo mediante articulo.fechaBaja
    @Transactional
    public void bajaArticulo(Long id) {
        try {
            Articulo articulo = encontrarArticulo(id);
            // Chequeamos que no esté dado de baja
            checkBaja(articulo);

            // Chequear que no tenga OC activas (Restricción del negocio)
            checkOrdenesActivas(articulo);

            // Chequeamos que el artículo no tenga stock (Restricción del negocio)
            if (articulo.getStock() > 0)
                throw new RuntimeException("No puede darse de baja un artículo que aún tiene stock");

            articulo.setFechaBaja(LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Obtiene toda la información del artículo para mostrarla en detalle
    @Transactional
    public DTOArticuloDetalle getArticuloDetalle(Long id) {
        try {
            //Encontrar artículo
            Articulo articulo = encontrarArticulo(id);

            //Obtener la info del modelo de inventario
            DTOArticuloModeloInventario dtoModelo = datosModeloInventario(articulo.getModeloInventario());
            MiniDTOProvPred miniDTOProvPred = datosProvPred(articulo);

            // Dar lo restante para el prox pedido si es de Lote Fijo
            float restanteProximoPedido = 0;
            if (dtoModelo instanceof DTOArticuloModeloLoteFijo)
                restanteProximoPedido = articulo.getStock() - ((DTOArticuloModeloLoteFijo) dtoModelo).getPuntoPedido();

            // Armar el listado de proveedores
            List<DTOArticuloProveedor> dtoArtProvs = new ArrayList<>();
            for (ArticuloProveedor artProv : articulo.getArtProveedores()){
                DTOProveedor dtoProv = new DTOProveedor(
                        artProv.getProveedor().getId(), artProv.getProveedor().getProveedorCod(),
                        artProv.getProveedor().getProveedorNombre(), artProv.getProveedor().getProveedorTelefono(),
                        artProv.getProveedor().getProveedorFechaBaja()
                );
                DTOArticuloProveedor dtoArtProv = new DTOArticuloProveedor(
                        artProv.getCostoCompra(), artProv.getCargoPedido(), artProv.getCostoPedido(),
                        artProv.getDemoraEntrega(), artProv.getPrecioUnitario(), artProv.getPuntoPedido(),
                        dtoProv
                );
                dtoArtProvs.add(dtoArtProv);
            }

            calcularCGI(articulo);


            DTOArticuloDetalle dto = new DTOArticuloDetalle(
                    articulo.getId(), articulo.getNombre(), articulo.getStock(), articulo.getDemanda(),
                    articulo.getDescripcion(), articulo.getCostoAlmacenamiento(), articulo.getNivelServicio(), articulo.getDemandaDesviacionEstandar(),
                    dtoModelo, restanteProximoPedido,
                    miniDTOProvPred.getProvId(), miniDTOProvPred.getProvNombre(),
                    calcularCGI(articulo),
                    dtoArtProvs

            );
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addProveedor(Articulo articulo, DTOArticuloAddProveedor dto) {
        try {
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(dto.getProveedorId());

            // Checkear que el artículo no esté dado de baja
            checkBaja(articulo);

            // Checkear que no esté ya agregado el proveedor
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            if (artProvs != null) {
                for (ArticuloProveedor artProv : artProvs) {
                    if (artProv.getProveedor() == proveedor) {
                        throw new Exception("El proveedor ya existe para este artículo");
                    }
                }
            } else
                artProvs = new ArrayList<>();      // Está por un warning que tiraba, pero andaba igual con o sin esta línea

            // Agregar ArtículoProveedor y setear todos sus datos
            ArticuloProveedor artProv = new ArticuloProveedor();

            artProv.setCostoCompra(dto.getCostoCompra());
            artProv.setCargoPedido(dto.getCargoPedido());
            artProv.setCostoPedido(dto.getCostoPedido());
            artProv.setDemoraEntrega(dto.getDemoraEntrega());
            artProv.setPrecioUnitario(dto.getPrecioUnitario());
            artProv.setProveedor(proveedor);
            artProvs.add(artProv);
            articulo.setArtProveedores(artProvs);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setProveedorPred(Articulo articulo, Long idProveedor) {
        try {

            if( idProveedor == 0){
                articulo.setProvPred(null);

                // Intentar calcular el lote optimo
                calcularLoteOptimo(articulo);

                // Intentar calcular el stock de seguridad
                calcularStockSeguridad(articulo);
                return;
            }
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(idProveedor);

            checkBaja(articulo);

            // Checkear que el proveedor esté agregado al artículo
            boolean provee = false;
            List<ArticuloProveedor> artProvs = articulo.getArtProveedores();
            for (ArticuloProveedor artProv : artProvs) {
                if (artProv.getProveedor() == proveedor) {
                    provee = true;
                    break;
                }
            }
            if (!provee)
                throw new Exception("El proveedor ingresado no provee este artículo");

            // Se settear como proveedor predeterminado
            articulo.setProvPred(proveedor);

            // Intentar calcular el lote optimo
            calcularLoteOptimo(articulo);

            // Intentar calcular el stock de seguridad
            calcularStockSeguridad(articulo);

            // Intentar calcular el punto de pedido
            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo) {
                calcularPuntoPedido(articulo);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // -------- Funciones auxiliares

    // Parte común entre el alta y el update de artículo. Info del artículo, el modelo de inventario
    private void commonAltaUpdate(Articulo articulo, DTOArticuloAlta dto){

        //Decidir modelo de inventario
        ArticuloModeloInventario modeloInventario = articulo.getModeloInventario();
        switch (dto.getModeloInventario().getNombre()){
            case "Lote Fijo" -> {
                // Si no era de lote fijo (por no existir o ser de intervalo fijo), se crea el nuevo modelo
                if (!(modeloInventario instanceof ArticuloModeloLoteFijo))
                    modeloInventario = new ArticuloModeloLoteFijo(); // Todos sus atributos son calculados con el prov pred
            }
            case "Intervalo Fijo" -> {
                // Si no era de intervalo fijo (por no existir o ser de lote fijo), se crea el nuevo modelo
                if (!(modeloInventario instanceof ArticuloModeloIntervaloFijo))
                    modeloInventario = new ArticuloModeloIntervaloFijo();

                // Setteamos sus atributos
                ((ArticuloModeloIntervaloFijo) modeloInventario).setFechaProximoPedido(dto.getModeloInventario().getFechaProxPedido());
                ((ArticuloModeloIntervaloFijo) modeloInventario).setIntervaloPedido(dto.getModeloInventario().getIntervaloPedido());
                ((ArticuloModeloIntervaloFijo) modeloInventario).setInventarioMaximo(dto.getModeloInventario().getInventarioMaximo());
            }
            default -> throw new RuntimeException("Modelo desconocido");
        }
        // Ponerle el modelo al artículo
        articulo.setModeloInventario(modeloInventario);

        // Settear la info de artículo
        articulo.setNombre(dto.getNombre());
        articulo.setDescripcion(dto.getDescripcion());
        articulo.setCostoAlmacenamiento(dto.getCostoAlmacenamiento());
        articulo.setDemanda(dto.getDemanda());
        articulo.setDemandaDesviacionEstandar(dto.getDesviacionEstandar());
        articulo.setNivelServicio(dto.getNivelServicio());
        articulo.setStock(dto.getStock());
    }

    // Encuentra un artículo que puede o no estar
    public Articulo encontrarArticulo(Long idArt) throws Exception {
        Optional<Articulo> optArticulo = articuloRepository.findById(idArt);
        if (optArticulo.isEmpty()) throw new Exception("No se encuentra el artículo");
        return optArticulo.get();
    }

    // Encuentra un proveedor que puede o no estar
    public Proveedor encontrarProveedor(Long idProv) throws Exception {
        Optional<Proveedor> optProveedor = proveedorRepository.findById(idProv);
        if (optProveedor.isEmpty() || optProveedor.get().getProveedorFechaBaja() != null) throw new Exception("No se encuentra el proveedor");
        return optProveedor.get();
    }

    //Arma un DTO de listado (tiene menos info)
    private DTOArticuloListado crearDtoListado(Articulo articulo) {

        // Obtenemos el nombre del modelo, la fecha (estimada) y la cantidad (estimada) del próximo pedido
        DTOArticuloModeloInventario dtoModelo = datosModeloInventario(articulo.getModeloInventario());
        MiniDTOProvPred miniDTOProvPred = datosProvPred(articulo);

        float cantidadProximoPedido = 0;
        LocalDateTime fechaProximo = LocalDateTime.now();

        if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo)
            cantidadProximoPedido = articulo.getStock() - ((ArticuloModeloLoteFijo) articulo.getModeloInventario()).getPuntoPedido();
        else
            fechaProximo = ((DTOArticuloModeloIntervaloFijo)dtoModelo).getFechaProximoPedido();

        // Creamos el dto en sí
        DTOArticuloListado dto = new DTOArticuloListado(
                articulo.getId(), articulo.getNombre(), articulo.getStock(),
                calcularCGI(articulo).getCgiTotal(),
                dtoModelo.getNombre(), fechaProximo , cantidadProximoPedido,
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
    private MiniDTOProvPred datosProvPred(Articulo articulo) {
        MiniDTOProvPred dto = new MiniDTOProvPred();
        Long provPredId = 0L;
        String provPredNom = "No hay proveedor predeterminado";

        // Chequeamos que el proveedor predeterminado exista
        if (articulo.getProvPred() == null) {
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

    // Retorna datos del modelo de inventario del artículo
    private DTOArticuloModeloInventario datosModeloInventario(ArticuloModeloInventario modeloInventario){
        DTOArticuloModeloInventario dto;

        String modeloNombre = "";
        // Obtener el nombre del modelo
        if (modeloInventario instanceof ArticuloModeloLoteFijo){
            dto = new DTOArticuloModeloLoteFijo(
                    modeloInventario.getLoteOptimo(),
                    ((ArticuloModeloLoteFijo) modeloInventario).getPuntoPedido()
            );
            dto.setNombre("Lote Fijo");
            dto.setStockSeguridad(modeloInventario.getStockSeguridad());
        }
        else {
            dto = new DTOArticuloModeloIntervaloFijo(
                    ((ArticuloModeloIntervaloFijo) modeloInventario).getIntervaloPedido(),
                    ((ArticuloModeloIntervaloFijo) modeloInventario).getInventarioMaximo(),
                    ((ArticuloModeloIntervaloFijo) modeloInventario).getFechaProximoPedido()
            );
            dto.setNombre("Intervalo Fijo");
            dto.setStockSeguridad(modeloInventario.getStockSeguridad());
        }

        return dto;
    }

    private Optional<ArticuloProveedor> obtenerArticuloProveedorPredeterminado(Articulo articulo) {
        Proveedor proveedorPredeterminado = articulo.getProvPred();

        if (proveedorPredeterminado == null) {
            return Optional.empty();
        }

        // Buscar el ArticuloProveedor cuyo proveedor coincida con el proveedor predeterminado
        return articulo.getArtProveedores()
                .stream()
                .filter(ap -> ap.getProveedor().equals(proveedorPredeterminado))
                .findFirst();
    }

    public float calcularZ(float nivelServicio) {
        // Validar que el nivel de servicio esté en el rango válido [0, 1]
        if (nivelServicio <= 0.5 || nivelServicio >= 1) {
            throw new IllegalArgumentException("El nivel de servicio debe estar entre 0.5 y 1 (excluyendo ambos extremos).");
        }

        // Usamos la distribución normal estándar para calcular el valor de z
        NormalDistribution normalDistribution = new NormalDistribution(0, 1); // Media 0, desviación estándar 1

        // Calcular el valor de z usando la inversa de la distribución normal
        return (float) normalDistribution.inverseCumulativeProbability(nivelServicio);
    }

    public int calcularLoteOptimo(Articulo articulo) throws RuntimeException{
        try {
            int loteOptimo;
            float demanda = articulo.getDemanda();
            float costoAlmacenamiento = articulo.getCostoAlmacenamiento();
            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                articulo.getModeloInventario().setLoteOptimo(0);
                return 0;
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            // Obtener el costo por pedido del proveedor
            float costoPorPedido = articuloProveedor.getCostoPedido();

            // Validar si alguno de los valores es 0
            if (demanda == 0 || costoAlmacenamiento == 0 || costoPorPedido == 0)
                throw new RuntimeException("Faltan valores para calcular el lote óptimo: demanda, costo de almacenamiento o costo por pedido son cero.");

            loteOptimo = (int) Math.round(Math.sqrt((2 * demanda * costoPorPedido) / costoAlmacenamiento));

            articulo.getModeloInventario().setLoteOptimo(loteOptimo);

            return loteOptimo;
        } catch (Exception e) {
            // Captura de cualquier otro tipo de excepción
            e.printStackTrace();
            return 0;
        }
    }

    public int calcularPuntoPedido(Articulo articulo) {
        try {

            // Obtener el modelo de inventario, que debe ser de tipo ArticuloModeloLoteFijo
            if (!(articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo)) return 0;

            ArticuloModeloLoteFijo modeloLoteFijo = (ArticuloModeloLoteFijo) articulo.getModeloInventario();

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                modeloLoteFijo.setPuntoPedido(0);
                return 0;
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            // Obtener el costo por pedido del proveedor
            int puntoPedido;
            float leadTime = articuloProveedor.getDemoraEntrega();
            float demanda = articulo.getDemanda();
            float stockSeguridad = articulo.getModeloInventario().getStockSeguridad();

            puntoPedido = Math.round(demanda * leadTime + stockSeguridad);

            modeloLoteFijo.setPuntoPedido(puntoPedido);

            return puntoPedido;
        } catch (Exception e) {
            // Captura de cualquier otro tipo de excepción
            e.printStackTrace();
            return 0;
        }
    }

    public int calcularStockSeguridad(Articulo articulo) {
        try {

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                articulo.getModeloInventario().setStockSeguridad(0);
                return 0;
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();
            float leadTime = articuloProveedor.getDemoraEntrega();
            float nivelServicio = articulo.getNivelServicio();
            float desviacion = articulo.getDemandaDesviacionEstandar();
            float z = calcularZ(nivelServicio);

            if (leadTime == 0 || z == 0 || desviacion == 0)
                return 0;

            int stockSeguridad;

            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo) {
                // Cálculo para Lote Fijo
                stockSeguridad = (int) Math.round(z * desviacion * Math.sqrt(leadTime));
                articulo.getModeloInventario().setStockSeguridad(stockSeguridad);

            } else if (articulo.getModeloInventario() instanceof ArticuloModeloIntervaloFijo) {
                // Cálculo para Intervalo Fijo
                ArticuloModeloIntervaloFijo modeloIntervaloFijo = (ArticuloModeloIntervaloFijo) articulo.getModeloInventario();

                int intervaloPedido = modeloIntervaloFijo.getIntervaloPedido();

                if (intervaloPedido == 0) {
                    return 0; // Si intervaloPedido es cero, no calculamos el stock de seguridad
                }
                // Calcular Stock de Seguridad para Intervalo Fijo
                stockSeguridad = (int) Math.round(z * desviacion * Math.sqrt(leadTime + intervaloPedido));
                articulo.getModeloInventario().setStockSeguridad(stockSeguridad);
            } else {
                // Si el modelo de inventario no es ni Lote Fijo ni Intervalo Fijo
                throw new IllegalArgumentException("Este artículo no tiene un modelo de inventario válido.");
            }

            return stockSeguridad;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public DTOCGI calcularCGI(Articulo articulo) {
        try {

            ArticuloModeloInventario modelo = articulo.getModeloInventario();
            int loteOptimo = calcularLoteOptimo(articulo);

            calcularStockSeguridad(articulo);
            if (modelo instanceof ArticuloModeloLoteFijo){
                calcularPuntoPedido(articulo);
            }

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return new DTOCGI(0,0,0,0);
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            float precio = articuloProveedor.getPrecioUnitario();
            float costoAlmacenamiento = articulo.getCostoAlmacenamiento();
            float costoPedido = articuloProveedor.getCostoPedido();
            float demanda= articulo.getDemanda();

            float cgiCCompra = precio * loteOptimo;
            float cgiCAalmacenamiento = costoAlmacenamiento * ((float) loteOptimo / 2);
            float cgiCPedido = costoPedido * (demanda / loteOptimo);
            float cgiCTotal = cgiCCompra + cgiCAalmacenamiento + cgiCPedido;

            if (loteOptimo == 0) {
                return new DTOCGI(0,0,0,0);
            }

            return new DTOCGI(cgiCTotal, cgiCCompra, cgiCAalmacenamiento, cgiCPedido);

        } catch (Exception e) {
            e.printStackTrace();
            return new DTOCGI(0,0,0,0);
        }


    }

    // Chequea que el artíuclo no esté de baja. En caso de estarlo, error
    public void checkBaja(Articulo articulo) throws RuntimeException {
        if (articulo.getFechaBaja() != null)
            throw new RuntimeException("El artículo está dado de baja");
    }

    // Chequea que el artíuclo no tenga OC activas. En caso de tenerlas, error
    private void checkOrdenesActivas(Articulo articulo) throws RuntimeException {
        List<OrdenCompra> ordenesCompra = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
        if (!ordenesCompra.isEmpty())
            throw new RuntimeException("Hay órdenes de compra activas, no puede darse de baja al artículo");
    }

    // Chequea que el proveedor que estamos intentando sacar no tenga órdenes activas en ese momento
    private boolean checkOredenesActivasArtProv(Articulo articulo, Proveedor proveedor){
        List<OrdenCompra> ordenesCompra = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
        if (ordenesCompra.isEmpty())
            return false;
        for (OrdenCompra oc : ordenesCompra){
            if (oc.getProveedor() == proveedor)
                return true;
        }
        return false;
    }

    public List<DTOArticuloGrupoProveedor> listarArticulosPorProveedor() {
        try {
            List<Articulo> articulos = articuloRepository.findByFechaBajaIsNullAndProvPredIsNotNull();
            Map<Long, DTOArticuloGrupoProveedor> mapaProveedores = new LinkedHashMap<>();

            for (Articulo articulo : articulos) {
                Proveedor provPred = articulo.getProvPred();

                for (ArticuloProveedor ap : articulo.getArtProveedores()) {
                    Proveedor proveedor = ap.getProveedor();
                    Long idProveedor = proveedor.getId();

                    // Si todavia no lo agregamos al map, lo creamos
                    mapaProveedores.putIfAbsent(idProveedor,
                            new DTOArticuloGrupoProveedor(idProveedor, proveedor.getProveedorNombre()));

                    // Verificamos si es proveedor predeterminado
                    boolean esPredeterminado = provPred != null && provPred.getId().equals(idProveedor);

                    // Creamos el artículo
                    DTOArticuloProveedorListado dto = new DTOArticuloProveedorListado(
                            articulo.getId(),
                            articulo.getNombre(),
                            ap.getPrecioUnitario(),
                            esPredeterminado,
                            articulo.getModeloInventario().getLoteOptimo()
                    );

                    // Agregamos el artículo al grupo del proveedor
                    mapaProveedores.get(idProveedor).getArticulos().add(dto);
                }
            }

            return new ArrayList<>(mapaProveedores.values());
        } catch (Exception e) {
            throw new RuntimeException("Error al listar artículos por proveedor: " + e.getMessage(), e);
        }
    }

    public List<DTOArticuloProveedorListado> listarArticulosPorProveedorId(Long idProveedor) {
        try {
            List<DTOArticuloProveedorListado> articulosDelProveedor = new ArrayList<>();

            Proveedor proveedor = proveedorRepository.findById(idProveedor)
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

            List<Articulo> articulos = articuloRepository.findArticulosActivosbyProveedor(proveedor);

            loopArts: for (Articulo articulo : articulos) {
                Proveedor provPred = articulo.getProvPred();
                for (ArticuloProveedor ap : articulo.getArtProveedores()) {
                    boolean esPredeterminado = false;
                    if (provPred != null && ap.getProveedor().getId().equals(idProveedor)) {
                        esPredeterminado = provPred.getId().equals(idProveedor);
                    }
                    DTOArticuloProveedorListado dto = new DTOArticuloProveedorListado(
                            articulo.getId(),
                            articulo.getNombre(),
                            ap.getPrecioUnitario(),
                            esPredeterminado,
                            articulo.getModeloInventario().getLoteOptimo()
                    );
                    articulosDelProveedor.add(dto);
                    continue loopArts;
                }
            }

            return articulosDelProveedor;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar artículos del proveedor: " + e.getMessage(), e);
        }
    }
    //listado artuculo a reponer
    @Transactional
    public List<DTOArticuloListado> listarProductosAReponer() {
        List<DTOArticuloListado> productosAReponer = new ArrayList<>();
        List<Articulo> articulos = articuloRepository.findAll();

        for (Articulo articulo : articulos) {
            if (articulo.getFechaBaja() != null)
                continue;
            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo modelo) {
                if (articulo.getStock() <= modelo.getPuntoPedido()) {
                    List<OrdenCompra> ordenesActivas = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
                    if (ordenesActivas.isEmpty()) {
                        DTOArticuloListado dto = crearDtoListado(articulo);
                        productosAReponer.add(dto);
                    }
                }
            }
            else  if (articulo.getModeloInventario() instanceof ArticuloModeloIntervaloFijo modelo) {
                if (modelo.getFechaProximoPedido().isBefore(LocalDateTime.now())) {
                    List<OrdenCompra> ordenesActivas = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
                    if (ordenesActivas.isEmpty()) {
                        DTOArticuloListado dto = crearDtoListado(articulo);
                        productosAReponer.add(dto);
                    }
                }
            }
        }
        return productosAReponer;
    }
    //Listado productos faltantes
    @Transactional
    public List<DTOArticuloFaltante> listarProductosFaltantes() {
        List<DTOArticuloFaltante> productosFaltantes = new ArrayList<>();
        List<Articulo> articulos = articuloRepository.findAll();

        for (Articulo articulo : articulos) {
            if (articulo.getFechaBaja() != null)
                continue;
            float stockSeguridad = articulo.getModeloInventario().getStockSeguridad();
            if (articulo.getStock() < stockSeguridad) {
                DTOArticuloListado dto = crearDtoListado(articulo);
                DTOArticuloFaltante dtoEntrega = new DTOArticuloFaltante(
                        dto.getId(), dto.getNombre(), dto.getStock(),
                        stockSeguridad, dto.getModeloInventario(), dto.getFechaProximoPedido(),
                        dto.getRestanteProximoPedido(), dto.getProveedorPredeterminadoId(), dto.getProveedorPredeterminadoNombre()
                );
                productosFaltantes.add(dtoEntrega);
            }
        }
        return productosFaltantes;
    }
    //Listado proveedores por articulo
    @Transactional
    public List<DTOProveedor> listarProveedoresPorArticulo(Long articuloId) throws Exception {
        Articulo articulo = encontrarArticulo(articuloId);
        List<DTOProveedor> proveedores = new ArrayList<>();

        for (ArticuloProveedor artProv : articulo.getArtProveedores()) {
            Proveedor proveedor = artProv.getProveedor();
            DTOProveedor dtoProveedor = new DTOProveedor(
                    proveedor.getId(),
                    proveedor.getProveedorCod(),
                    proveedor.getProveedorNombre(),
                    proveedor.getProveedorTelefono(),
                    proveedor.getProveedorFechaBaja()
            );
            proveedores.add(dtoProveedor);
        }
        return proveedores;
    }
    //Ajuste de inventario
    @Transactional
    public void ajustarInventario(Long articuloId, float nuevaCantidad) throws Exception {
        Articulo articulo = encontrarArticulo(articuloId);
        articulo.setStock(nuevaCantidad);
        articuloRepository.save(articulo);
    }

}
