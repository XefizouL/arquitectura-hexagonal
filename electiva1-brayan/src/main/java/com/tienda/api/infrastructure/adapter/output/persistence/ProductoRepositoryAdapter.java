// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence;

import com.tienda.api.domain.exception.ConflictoException;
import com.tienda.api.domain.model.Producto;
import com.tienda.api.domain.port.out.ProductoRepositoryPort;
import com.tienda.api.infrastructure.adapter.output.persistence.mapper.PersistenciaMapper;
import com.tienda.api.infrastructure.adapter.output.persistence.repository.ProductoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {

    private final ProductoJpaRepository jpa;

    public ProductoRepositoryAdapter(ProductoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Producto> buscarTodos() {
        return jpa.findAll().stream().map(PersistenciaMapper::aDominio).toList();
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::aDominio);
    }

    @Override
    public Producto guardar(Producto producto) {
        return PersistenciaMapper.aDominio(jpa.save(PersistenciaMapper.aEntidad(producto)));
    }

    @Override
    public void eliminar(Producto producto) {
        try {
            jpa.deleteById(producto.getId());
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictoException("El registro está asociado a uno o más pedidos");
        }
    }
}
