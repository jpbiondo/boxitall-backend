package com.boxitall.boxitall.mappers;

import com.boxitall.boxitall.dtos.articulo.DTOArticuloAddProveedor;
import com.boxitall.boxitall.entities.ArticuloProveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ArticuloMapper {

    public abstract List<ArticuloProveedor> dtoAltaProvsArtToArtProvs(List<DTOArticuloAddProveedor> dtoAltaProveedorArticulo);

    @Mapping(target = "proveedor", ignore = true)
    public abstract ArticuloProveedor dtoAltaProvArtToArtProv(DTOArticuloAddProveedor dtoAltaProveedorArticulo);
}
