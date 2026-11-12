// by from ia
package com.tienda.api.infrastructure.adapter.input.rest.dto;

import java.util.List;

public record PedidoRequest(String cliente, List<ItemRequest> items) {
}
