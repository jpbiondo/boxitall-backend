package com.boxitall.boxitall.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "proveedor")
public class Proveedor extends BaseEntity {
    @Column(name = "cod_proveedor")
    private int proveedorCod;

    @Column(name = "nombre_proveedor")
    private String proveedorNombre;

    @Column(name = "telefono_proveedor")
    private String proveedorTelefono;

    @Column(name = "FechaBaja_proveedor")
    private LocalDateTime proveedorFechaBaja;

}
