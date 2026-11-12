// by from ia
package com.tienda.api.domain.port.out;

import com.tienda.api.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    List<Producto> buscarTodos();

    Optional<Producto> buscarPorId(Long id);

    Producto guardar(Producto producto);

    void eliminar(Producto producto);
}
