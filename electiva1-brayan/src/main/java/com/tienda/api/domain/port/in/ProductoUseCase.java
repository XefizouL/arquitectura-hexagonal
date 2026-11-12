// by from ia
package com.tienda.api.domain.port.in;

import com.tienda.api.domain.model.Producto;

import java.math.BigDecimal;
import java.util.List;

public interface ProductoUseCase {

        record ProductoCommand(String nombre, String descripcion, BigDecimal precio, Integer stock) {
    }

    List<Producto> listarTodos();

    Producto buscarPorId(Long id);

    Producto crear(ProductoCommand comando);

    Producto actualizar(Long id, ProductoCommand comando);

    void eliminar(Long id);
}
