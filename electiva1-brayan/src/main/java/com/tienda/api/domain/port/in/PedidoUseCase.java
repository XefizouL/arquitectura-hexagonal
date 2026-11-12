// by from ia
package com.tienda.api.domain.port.in;

import com.tienda.api.domain.model.Pedido;

import java.util.List;

public interface PedidoUseCase {

        record ItemCommand(Long productoId, Integer cantidad) {
    }

    record PedidoCommand(String cliente, List<ItemCommand> items) {
    }

    List<Pedido> listarTodos();

    Pedido buscarPorId(Long id);

    Pedido crear(PedidoCommand comando);

    Pedido actualizar(Long id, PedidoCommand comando);

    Pedido agregarItem(Long id, ItemCommand item);

    Pedido quitarItem(Long id, Long itemId);

    Pedido confirmar(Long id);

    void eliminar(Long id);
}
