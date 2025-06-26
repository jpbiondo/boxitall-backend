package com.boxitall.boxitall.mappers;

import com.boxitall.boxitall.dtos.proveedor.DTOProveedor;
import com.boxitall.boxitall.entities.Proveedor;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ProveedorMapper {

    public abstract List<DTOProveedor> proveedoresToDto(List<Proveedor> proveedores);
}
