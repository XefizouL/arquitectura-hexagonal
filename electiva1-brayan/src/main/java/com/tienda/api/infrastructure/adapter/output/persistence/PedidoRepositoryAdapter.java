// by from ia
package com.tienda.api.infrastructure.adapter.output.persistence;

import com.tienda.api.domain.model.Pedido;
import com.tienda.api.domain.port.out.PedidoRepositoryPort;
import com.tienda.api.infrastructure.adapter.output.persistence.mapper.PersistenciaMapper;
import com.tienda.api.infrastructure.adapter.output.persistence.repository.PedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository jpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Pedido> buscarTodos() {
        return jpa.findAll().stream().map(PersistenciaMapper::aDominio).toList();
    }

    @Override
    public Optional<Pedido> buscarPorId(Long id) {
        return jpa.findById(id).map(PersistenciaMapper::aDominio);
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        return PersistenciaMapper.aDominio(jpa.save(PersistenciaMapper.aEntidad(pedido)));
    }

    @Override
    public void eliminar(Pedido pedido) {
        jpa.deleteById(pedido.getId());
    }
}
