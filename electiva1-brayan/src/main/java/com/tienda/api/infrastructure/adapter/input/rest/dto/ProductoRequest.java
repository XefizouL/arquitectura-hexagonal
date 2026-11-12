// by from ia
package com.tienda.api.infrastructure.adapter.input.rest.dto;

import java.math.BigDecimal;

public record ProductoRequest(String nombre, String descripcion, BigDecimal precio, Integer stock) {
}
