// by from ia
package com.tienda.api.infrastructure.adapter.input.rest.dto;

import com.tienda.api.domain.model.DetallePedido;
import com.tienda.api.domain.model.EstadoPedido;
import com.tienda.api.domain.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, String cliente, LocalDateTime fecha, EstadoPedido estado,
                             BigDecimal total, List<ItemResponse> items) {

    public record ItemResponse(Long id, ProductoResponse producto, Integer cantidad,
                               BigDecimal precioUnitario, BigDecimal subtotal) {

        static ItemResponse desde(DetallePedido d) {
            return new ItemResponse(d.getId(), ProductoResponse.desde(d.getProducto()), d.getCantidad(),
                    d.getPrecioUnitario(), d.getSubtotal());
        }
    }

    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(p.getId(), p.getCliente(), p.getFecha(), p.getEstado(), p.getTotal(),
                p.getItems().stream().map(ItemResponse::desde).toList());
    }
}
