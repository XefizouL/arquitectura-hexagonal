// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence.mapper;

import com.tienda.api.domain.model.DetallePedido;
import com.tienda.api.domain.model.Pedido;
import com.tienda.api.domain.model.Producto;
import com.tienda.api.infrastructure.adapter.output.persistence.entity.DetallePedidoEntity;
import com.tienda.api.infrastructure.adapter.output.persistence.entity.PedidoEntity;
import com.tienda.api.infrastructure.adapter.output.persistence.entity.ProductoEntity;

import java.util.ArrayList;
import java.util.List;

public final class PersistenciaMapper {

    private PersistenciaMapper() {
    }

    public static Producto aDominio(ProductoEntity e) {
        return new Producto(e.getId(), e.getNombre(), e.getDescripcion(), e.getPrecio(), e.getStock());
    }

    public static ProductoEntity aEntidad(Producto p) {
        return new ProductoEntity(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock());
    }

    public static Pedido aDominio(PedidoEntity e) {
        List<DetallePedido> items = new ArrayList<>();
        for (DetallePedidoEntity d : e.getItems()) {
            items.add(new DetallePedido(d.getId(), aDominio(d.getProducto()), d.getCantidad(),
                    d.getPrecioUnitario(), d.getSubtotal()));
        }
        return new Pedido(e.getId(), e.getCliente(), e.getFecha(), e.getEstado(), e.getTotal(), items);
    }

    public static PedidoEntity aEntidad(Pedido p) {
        PedidoEntity entidad = PedidoEntity.builder()
                .id(p.getId())
                .cliente(p.getCliente())
                .fecha(p.getFecha())
                .estado(p.getEstado())
                .total(p.getTotal())
                .build();
        for (DetallePedido d : p.getItems()) {
            entidad.getItems().add(DetallePedidoEntity.builder()
                    .id(d.getId())
                    .pedido(entidad)
                    .producto(aEntidad(d.getProducto()))
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(d.getSubtotal())
                    .build());
        }
        return entidad;
    }
}
