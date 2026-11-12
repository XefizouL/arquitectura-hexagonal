// by from ia
package com.tienda.api.domain.port.out;

import com.tienda.api.domain.model.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {

    List<Pedido> buscarTodos();

    Optional<Pedido> buscarPorId(Long id);

    Pedido guardar(Pedido pedido);

    void eliminar(Pedido pedido);
}
