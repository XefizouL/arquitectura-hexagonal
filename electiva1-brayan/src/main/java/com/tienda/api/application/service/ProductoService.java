// by from ia
package com.tienda.api.application.service;

import com.tienda.api.domain.exception.RecursoNoEncontradoException;
import com.tienda.api.domain.model.Producto;
import com.tienda.api.domain.port.in.ProductoUseCase;
import com.tienda.api.domain.port.out.ProductoRepositoryPort;

import java.util.List;

public class ProductoService implements ProductoUseCase {

    private final ProductoRepositoryPort productoRepository;

    public ProductoService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.buscarTodos();
    }

    @Override
    public Producto buscarPorId(Long id) {
        return productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    @Override
    public Producto crear(ProductoCommand c) {
        Producto producto = new Producto(null, c.nombre(), c.descripcion(), c.precio(), c.stock());
        return productoRepository.guardar(producto);
    }

    @Override
    public Producto actualizar(Long id, ProductoCommand c) {
        Producto existente = buscarPorId(id);
        existente.actualizarDatos(c.nombre(), c.descripcion(), c.precio(), c.stock());
        return productoRepository.guardar(existente);
    }

    @Override
    public void eliminar(Long id) {
        productoRepository.eliminar(buscarPorId(id));
    }
}
