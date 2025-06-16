package com.boxitall.boxitall.services;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAlta;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloDetalle;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloListado;
import com.boxitall.boxitall.dtos.articulo.DTOArticuloProveedor;
import com.boxitall.boxitall.entities.*;
import com.boxitall.boxitall.repositories.ArticuloRepository;
import com.boxitall.boxitall.repositories.OrdenCompraArticuloRepository;
import com.boxitall.boxitall.repositories.ProveedorRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Service
public class ArticuloService extends BaseEntityServiceImpl<Articulo, Long> {
    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private OrdenCompraArticuloRepository ordenCompraArticuloRepository;

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
                    modeloInventario = new ArticuloModeloLoteFijo();
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
    public void addProveedor(Long idProveedor, Long idArt, DTOArticuloProveedor dto){
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

            // Agregar ArtículoProveedor y setear todos sus datos
            ArticuloProveedor artProv = new ArticuloProveedor();

            artProv.setCostoCompra(dto.getCostoCompra());
            artProv.setCargoPedido(dto.getCargoPedido());
            artProv.setCostoPedido(dto.getCostoPedido());
            artProv.setDemoraEntrega(dto.getDemoraEntrega());
            artProv.setPrecioUnitario(dto.getPrecioUnitario());
            artProv.setPuntoPedido(dto.getPuntoPedido());


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

            /*calcularLoteOptimo(articulo);
            calcularStockSeguridad(articulo);
            calcularPuntoPedido(articulo);
            calcularCGI(articulo);*/
            // Intentar calcular el lote óptimo
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
                Optional<Integer> puntoPedido = calcularPuntoPedido(articulo);
                if (puntoPedido.isEmpty()) {
                    throw new RuntimeException("No se pudo calcular el punto de pedido.");
                }
            }

            // Intentar calcular el Costo de Gestión de Inventarios (CGI)
            Optional<Double> cgi = calcularCGI(articulo);
            if (cgi.isEmpty()) {
                throw new RuntimeException("No se pudo calcular el Costo de Gestión de Inventarios (CGI).");
            }

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


            loteOptimo =Math.round(Math.sqrt((2 * demanda * costoPorPedido)/ costoAlmacenamiento));

            articulo.getModeloInventario().setLoteOptimo((int)loteOptimo);
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
    public Optional<Integer> calcularPuntoPedido(Articulo articulo) {
        try {

            // Obtener el modelo de inventario, que debe ser de tipo ArticuloModeloLoteFijo

            ArticuloModeloLoteFijo modeloLoteFijo = (ArticuloModeloLoteFijo) articulo.getModeloInventario();

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return Optional.empty();
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            // Obtener el costo por pedido del proveedor
            float puntoPedido;
            float leadTime = articuloProveedor.getDemoraEntrega();
            float demanda = articulo.getDemanda();
            float stockSeguridad= articulo.getModeloInventario().getStockSeguridad();

            puntoPedido =Math.round(demanda * leadTime + stockSeguridad) ;

            modeloLoteFijo.setPuntoPedido((int)puntoPedido);
            save(articulo);

            return Optional.of((int) puntoPedido);

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
            float z =calcularZ(nivelServicio);

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
            }
              else {
                    // Si el modelo de inventario no es ni Lote Fijo ni Intervalo Fijo
                    throw new IllegalArgumentException("Este artículo no tiene un modelo de inventario válido.");
              }

              return Optional.of(stockSeguridad);

            } catch(Exception e){
                e.printStackTrace();
                return Optional.empty();
            }
        }

    public Optional<Double> calcularCGI(Articulo articulo) {
        try {

            float loteOptimo = articulo.getModeloInventario().getLoteOptimo();

            Optional<ArticuloProveedor> articuloProveedorPred = obtenerArticuloProveedorPredeterminado(articulo);

            // Si no se puede obtener el ArticuloProveedor, devolvemos Optional.empty()
            if (articuloProveedorPred.isEmpty()) {
                return Optional.empty();
            }

            ArticuloProveedor articuloProveedor = articuloProveedorPred.get();

            double precio = articuloProveedor.getPrecioUnitario();
            double costoAlmacenamiento = articulo.getCostoAlmacenamiento();
            double costoPedido = articuloProveedor.getCostoPedido();
            double demanda= articulo.getDemanda();

            // Calcula el CGI utilizando la fórmula
            double cgi = (precio * loteOptimo)
                    + (costoAlmacenamiento * (loteOptimo / 2))
                    + (costoPedido * (demanda / loteOptimo));//REVISAR, CANTIDAD VS LOTE_OPTIMO

            return Optional.of(cgi);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }


    }
    //Falta calculo de intervalo Fijo

}
