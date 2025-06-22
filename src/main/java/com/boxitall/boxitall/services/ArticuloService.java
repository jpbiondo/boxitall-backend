package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.articulo.*;
import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.dtos.articulo.*;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Transactional
    public void altaArticulo(DTOArticuloAlta dto) {
        try {
            List<Articulo> articulos = articuloRepository.findAll(); //Encuentra todos los artículos
            for (Articulo articulo : articulos) {
                if (Objects.equals(articulo.getNombre(), dto.getNombre()) && articulo.getFechaBaja() != null)
                    throw new RuntimeException("Ya existe un artículo con este nombre");
            }

            //Decidir modelo de inventario
            ArticuloModeloInventario modeloInventario;
            switch (dto.getModeloInventario().getNombre()){
                case "Lote Fijo" -> {
                    modeloInventario = new ArticuloModeloLoteFijo(); // Todos sus atributos son calculados con el prov pred
                }
                case "Intervalo Fijo" -> {
                    System.out.println(dto.getModeloInventario().getFechaProxPedido());
                    LocalDateTime proxPedido = LocalDateTime.parse(dto.getModeloInventario().getFechaProxPedido()+"T00:00:00");
                    modeloInventario = new ArticuloModeloIntervaloFijo(
                            proxPedido,
                            dto.getModeloInventario().getIntervaloPedido(),
                            dto.getModeloInventario().getInventarioMaximo()
                    );
                }
                default -> throw new RuntimeException("Modelo desconocido");
            }

            // Crear artículo
            Articulo articulo = new Articulo(
                    dto.getNombre(), dto.getDescripcion(),dto.getCostoAlmacenamiento(),
                    dto.getDemanda(), dto.getDesviacionEstandar(),dto.getNivelServicio(),
                    dto.getStock(), modeloInventario
            );

            // Guardar el artículo
            Articulo savedArticulo = articuloRepository.save(articulo);

            // Esto es completamente ineficiente, tendría que agregar los proveedores y después guardarlo para que se haga junto,
            // pero eso implicaría romper el alta de proveedor así que ya está

            // Agregar los proveedores
            for (DTOArticuloAddProveedor dtoArtProv : dto.getArticuloProveedores()){
                addProveedor(dtoArtProv);
            }

            // Agregar prov pred
            if( dto.getProvPredId() != null){
                setProveedorPred(dto.getProvPredId(), savedArticulo.getId());
            }

        } catch (Exception e) {
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


            DTOArticuloDetalle dto = new DTOArticuloDetalle(
                    articulo.getId(), articulo.getNombre(), articulo.getStock(), articulo.getDemanda(),
                    articulo.getDescripcion(), articulo.getCostoAlmacenamiento(), articulo.getNivelServicio(),
                    dtoModelo, restanteProximoPedido,
                    miniDTOProvPred.getProvId(), miniDTOProvPred.getProvNombre(),
                    10,10,10,10,10, // TODO - CGI
                    dtoArtProvs

            );
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    //public void addProveedor(Long idProveedor, Long idArt, DTOArticuloProveedor dto){
    public void addProveedor(DTOArticuloAddProveedor dto) {
        try {
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(dto.getArticuloId());
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(dto.getProveedorId());

            // Checkear que el artículo no esté dado de baja
            checkBaja(articulo);

            //checkear que no esté ya agregado el proveedor
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

            // Guardar cambios
            update(dto.getArticuloId(), articulo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void setProveedorPred(Long idProveedor, Long idArt) {
        try {
            //Encontrar el Artículo
            Articulo articulo = encontrarArticulo(idArt);
            //Encontrar el Proveedor
            Proveedor proveedor = encontrarProveedor(idProveedor);

            checkBaja(articulo);

            //checkear que ya esté agregado el proveedor
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

            if (articulo.getProvPred() == proveedor)
                throw new RuntimeException("El proveedor ingresado ya es proveedor predeterminado de este artículo");

            //Settear proveedor
            articulo.setProvPred(proveedor);

            // Intentar calcular el lote optimo
            Optional<Integer> loteOptimo = calcularLoteOptimo(articulo);
            if (loteOptimo.isEmpty()) {
                throw new RuntimeException("No se pudo calcular el lote óptimo.");
            }

            // Intentar calcular el stock de seguridad
            Optional<Integer> stockSeguridad = calcularStockSeguridad(articulo);
            if (stockSeguridad.isEmpty()) {
                throw new RuntimeException("No se pudo calcular el stock de seguridad.");
            }

            // Intentar calcular el punto de pedido
            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo) {
                float puntoPedido = calcularPuntoPedido(articulo);
//                if (puntoPedido.isEmpty()) {
//                    throw new RuntimeException("No se pudo calcular el punto de pedido.");
//                }
            }

            // Intentar calcular el Costo de Gestión de Inventarios (CGI)
            float cgi = calcularCGI(articulo);
//            if (cgi.isEmpty()) {
//                throw new RuntimeException("No se pudo calcular el Costo de Gestión de Inventarios (CGI).");
//            }

            //Guardar cambios
            update(idArt, articulo);


        } catch (Exception e) {
            throw new RuntimeException("No se pudo establecer el proveedor predeterminado");
        }
    }

    @Transactional
    public void quitarProveedor(Long idProveedor, Long idArt) {
        try {
            Articulo articulo = encontrarArticulo(idArt);
            Proveedor proveedor = encontrarProveedor(idProveedor);

            checkBaja(articulo);

            // Encontrar el artículoProveedor de ese proveedor
            ArticuloProveedor articuloProveedor = null;
            for (ArticuloProveedor artProv : articulo.getArtProveedores()) {
                if (artProv.getProveedor() == proveedor) {
                    articuloProveedor = artProv;
                    break;
                }
            }
            if (articuloProveedor == null) throw new RuntimeException("El proveedor ingresado no provee este artículo");

            articulo.getArtProveedores().remove(articuloProveedor);
            articulo.setProvPred(null);
            update(idArt, articulo);

        } catch (Exception e) {
            throw new RuntimeException(e);
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
                calcularCGI(articulo),
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

    //Un mini DTO del modelo de inventario para usar dentro de este service
    @Setter
    @Getter
    private class MiniDTOModeloInventario {
        private String modeloNombre;
        private LocalDateTime fechaProxPedido;
        private float cantProxPedido; // Básicamente, lote óptimo
    }

    // Retorna el nombre del modelo, la fecha (estimada) y la cantidad (estimada) del próximo pedido
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

    @Transactional
    public Optional<Integer> calcularLoteOptimo(Articulo articulo) {
        try {
            double loteOptimo;
            float demanda = articulo.getDemanda();
            float costoAlmacenamiento = articulo.getCostoAlmacenamiento();
            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return Optional.empty();
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();
            // Obtener el costo por pedido del proveedor
            float costoPorPedido = articuloProveedor.getCostoPedido();

            // Validar si alguno de los valores es 0
            if (demanda == 0 || costoAlmacenamiento == 0 || costoPorPedido == 0) {

                throw new IllegalArgumentException("Faltan valores para calcular el lote óptimo: demanda, costo de almacenamiento o costo por pedido son cero.");
            }


            loteOptimo = Math.round(Math.sqrt((2 * demanda * costoPorPedido) / costoAlmacenamiento));

            articulo.getModeloInventario().setLoteOptimo((int) loteOptimo);
            save(articulo);

            return Optional.of((int) loteOptimo);

        } catch (IllegalArgumentException e) {
            // Excepción para modelo de inventario incorrecto
            System.err.println(e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            // Captura de cualquier otro tipo de excepción
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Transactional
    public float calcularPuntoPedido(Articulo articulo) {
        try {

            // Obtener el modelo de inventario, que debe ser de tipo ArticuloModeloLoteFijo
            if (!(articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo)) return 0;

            ArticuloModeloLoteFijo modeloLoteFijo = (ArticuloModeloLoteFijo) articulo.getModeloInventario();

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return 0;
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            // Obtener el costo por pedido del proveedor
            float puntoPedido;
            float leadTime = articuloProveedor.getDemoraEntrega();
            float demanda = articulo.getDemanda();
            float stockSeguridad = articulo.getModeloInventario().getStockSeguridad();

            puntoPedido = Math.round(demanda * leadTime + stockSeguridad);

            modeloLoteFijo.setPuntoPedido((int) puntoPedido);
            save(articulo);

            return puntoPedido;

        } catch (IllegalArgumentException e) {
            // Excepción para modelo de inventario incorrecto
            System.err.println(e.getMessage());
            return 0;
        } catch (Exception e) {
            // Captura de cualquier otro tipo de excepción
            e.printStackTrace();
            return 0;
        }
    }

    @Transactional
    public Optional<Integer> calcularStockSeguridad(Articulo articulo) {
        try {

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return Optional.empty();
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();
            float leadTime = articuloProveedor.getDemoraEntrega();
            float nivelServicio = articulo.getNivelServicio();
            float desviacion = articulo.getDemandaDesviacionEstandar();
            float z = calcularZ(nivelServicio);

            if (leadTime == 0 || z == 0 || desviacion == 0) {
                return Optional.empty();
            }

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
                    return Optional.empty(); // Si intervaloPedido es cero, no calculamos el stock de seguridad
                }
                // Calcular Stock de Seguridad para Intervalo Fijo
                stockSeguridad = (int) Math.round(z * desviacion * Math.sqrt(leadTime + intervaloPedido));
                articulo.getModeloInventario().setStockSeguridad(stockSeguridad);
            } else {
                // Si el modelo de inventario no es ni Lote Fijo ni Intervalo Fijo
                throw new IllegalArgumentException("Este artículo no tiene un modelo de inventario válido.");
            }

            return Optional.of(stockSeguridad);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public float calcularCGI(Articulo articulo) {
        try {

            float loteOptimo = articulo.getModeloInventario().getLoteOptimo();

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return 0;
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            float precio = articuloProveedor.getPrecioUnitario();
            float costoAlmacenamiento = articulo.getCostoAlmacenamiento();
            float costoPedido = articuloProveedor.getCostoPedido();
            float demanda= articulo.getDemanda();

            // Calcula el CGI utilizando la fórmula
            float cgi = (precio * loteOptimo)
                    + (costoAlmacenamiento * (loteOptimo / 2))
                    + (costoPedido * (demanda / loteOptimo));//REVISAR, CANTIDAD VS LOTE_OPTIMO

            if (loteOptimo == 0) cgi = 0;

            return cgi;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }


    }

    //Falta calculo de intervalo Fijo

    // Chequea que el artíuclo no esté de baja. En caso de estarlo, error
    private void checkBaja(Articulo articulo) throws RuntimeException {
        if (articulo.getFechaBaja() != null)
            throw new RuntimeException("El artículo está dado de baja");
    }

    // Chequea que el artíuclo no tenga OC activas. En caso de tenerlas, error
    private void checkOrdenesActivas(Articulo articulo) throws RuntimeException {
        List<OrdenCompra> ordenesCompra = ordenCompraRepository.findOrdenesActivasByArticulo(articulo);
        if (!ordenesCompra.isEmpty())
            throw new RuntimeException("Hay órdenes de compra activas, no puede darse de baja al artículo");
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
            for (Articulo articulo : articulos) {
                Proveedor provPred = articulo.getProvPred();
                for (ArticuloProveedor ap : articulo.getArtProveedores()) {
                    if (ap.getProveedor().getId().equals(idProveedor)) {
                        boolean esPredeterminado = provPred.getId().equals(idProveedor);

                        DTOArticuloProveedorListado dto = new DTOArticuloProveedorListado(
                                articulo.getId(),
                                articulo.getNombre(),
                                ap.getPrecioUnitario(),
                                esPredeterminado,
                                articulo.getModeloInventario().getLoteOptimo()
                        );

                        articulosDelProveedor.add(dto);
                    }
                }
            }

            return articulosDelProveedor;

        } catch (Exception e) {
            throw new RuntimeException("Error al listar artículos del proveedor: " + e.getMessage(), e);
        }
    }
    //listado artuculo a reponer
    @Transactional
    public List<DTOArticuloListado> listarProductosAReponer() {
        List<DTOArticuloListado> productosAReponer = new ArrayList<>();
        List<Articulo> articulos = articuloRepository.findAll();

        for (Articulo articulo : articulos) {
            if (articulo.getModeloInventario() instanceof ArticuloModeloLoteFijo) {
                ArticuloModeloLoteFijo modelo = (ArticuloModeloLoteFijo) articulo.getModeloInventario();
                if (articulo.getStock() <= modelo.getPuntoPedido()) {
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
    public List<DTOArticuloListado> listarProductosFaltantes() {
        List<DTOArticuloListado> productosFaltantes = new ArrayList<>();
        List<Articulo> articulos = articuloRepository.findAll();

        for (Articulo articulo : articulos) {
            float stockSeguridad = articulo.getModeloInventario().getStockSeguridad();
            if (articulo.getStock() <= stockSeguridad) {
                DTOArticuloListado dto = crearDtoListado(articulo);
                productosFaltantes.add(dto);
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
