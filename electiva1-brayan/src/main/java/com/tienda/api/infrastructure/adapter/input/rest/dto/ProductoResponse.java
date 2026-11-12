// by from ia
package com.tienda.api.infrastructure.adapter.input.rest.dto;

import com.tienda.api.domain.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock());
    }
}
